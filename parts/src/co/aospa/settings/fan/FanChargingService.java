/*
 * Copyright (C) 2024 Paranoid Android
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package co.aospa.settings.fan;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.provider.Settings;
import android.widget.Switch;
import android.util.Log;

import com.android.settingslib.fuelgauge.BatteryStatus;
import com.android.settingslib.collapsingtoolbar.R;

import co.aospa.settings.utils.FileUtils;
import co.aospa.settings.utils.SettingsUtils;

import co.aospa.settings.fan.FanFragment;

public class FanChargingService extends Service {
    private static final String TAG = "FanChargingService";
    private static final boolean DEBUG = true;

    public static final String FAST_CHARGE_PATH = "/sys/class/qcom-battery/oem_charger_type";
    public static final int FAST_CHARGE_TYPE_NORMAL = 1;
    public static final int FAST_CHARGE_TYPE_FAST = 7;

    private BroadcastReceiver mBatteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (DEBUG) Log.d(TAG, "onReceive");
            setChargerFanMode(isFastCharging(intent));
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (DEBUG) Log.d(TAG, "onStartCommand");
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (DEBUG) Log.d(TAG, "onCreate");
        Intent intent = registerReceiver(mBatteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        setChargerFanMode(isFastCharging(intent));
    }

    @Override
    public void onDestroy() {
        if (DEBUG) Log.d(TAG, "onDestroy");
        unregisterReceiver(mBatteryReceiver);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void setChargerFanMode(boolean fastCharging) {
        if (DEBUG) Log.d(TAG, "setChargerFanMode: " + fastCharging);
        if (SettingsUtils.getEnabled(this, FanFragment.KEY_FAN_ENABLE_FAST_CHARGE)) {
            if (DEBUG) Log.d(TAG, "setChargerFanMode: enabled, " + SettingsUtils.getEnabled(this, FanFragment.KEY_FAN_ENABLE));
            FanFragment.setFanStatus(fastCharging || SettingsUtils.getEnabled(this, FanFragment.KEY_FAN_ENABLE));
        } else if (DEBUG){
            Log.d(TAG, "setChargerFanMode: disabled");
        }
    }

    private boolean isFastCharging(Intent batteryIntent) {
        final BatteryStatus batteryStatus = new BatteryStatus(batteryIntent);

        if (DEBUG) {
            Log.d(TAG, "isFastCharging: " + batteryStatus);
            Log.d(TAG, "isFastCharging: " + FAST_CHARGE_PATH + ": " + FileUtils.readOneLine(FAST_CHARGE_PATH));
        }

        int oem_charger_type = Integer.parseInt(FileUtils.readOneLine(FAST_CHARGE_PATH));

        return batteryStatus.isPluggedIn() && !batteryStatus.isCharged()
               && (/*batteryStatus.getChargingSpeed(this) == BatteryStatus.CHARGING_FAST ||*/
                   oem_charger_type == FAST_CHARGE_TYPE_NORMAL || oem_charger_type == FAST_CHARGE_TYPE_FAST);
    }
}
