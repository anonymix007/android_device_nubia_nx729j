/*
 * Copyright (C) 2015 The CyanogenMod Project
 *               2017-2019 The LineageOS Project
 *               2024 Paranoid Android
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

package co.aospa.settings;

import android.media.AudioManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import co.aospa.settings.buttons.TriggersFragment;
import co.aospa.settings.fan.FanFragment;
import co.aospa.settings.gamekey.GameKeyFragment;

import co.aospa.settings.utils.FileUtils;
import co.aospa.settings.utils.SettingsUtils;

public class BootCompletedReceiver extends BroadcastReceiver {

    private static final boolean DEBUG = false;
    private static final String TAG = "NubiaParts";

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (DEBUG) Log.d(TAG, "Received boot completed intent");
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        int intValue = Integer.parseInt(sharedPreferences.getString(FanFragment.KEY_FAN_MODE, String.valueOf(FanFragment.FAN_AUTO_VALUE)));

        if (SettingsUtils.getEnabled(context, FanFragment.KEY_FAN_ENABLE)) {
            if (intValue == FanFragment.FAN_AUTO_VALUE) {
                FileUtils.writeLine(FanFragment.ENABLE, "1");
            } else if (intValue == FanFragment.FAN_MANUAL_VALUE) {
                String fanSpeed = String.valueOf(SettingsUtils.getInt(context, FanFragment.KEY_FAN_MANUAL, 1));
                FileUtils.writeLine(FanFragment.SPEED_LEVEL, fanSpeed);
            }
        }

        Boolean disabled = sharedPreferences.getBoolean(TriggersFragment.KEY_TRIGGERS_DISABLE, true);
        if (!disabled) {
            FileUtils.writeLine(TriggersFragment.UP_TOUCH_KEY_MODE_OPERATION, TriggersFragment.MODE_ENABLE);
            FileUtils.writeLine(TriggersFragment.DN_TOUCH_KEY_MODE_OPERATION, TriggersFragment.MODE_ENABLE);
        }

        String gamekeyStatus = FileUtils.readOneLine(KeyHandler.GAMEKEY_STATUS_PATH);

        if (gamekeyStatus == null) {
            Log.e(TAG, "Cannot get gameswitch status");
        } else if (SettingsUtils.getEnabled(context, GameKeyFragment.KEY_GAMEKEY_ENABLE)) {
            GameKeyFragment.loadGameKeySysPropsFromPrefs(context);
            AudioManager audioManager = context.getSystemService(AudioManager.class);
            if (gamekeyStatus.equals("1")) {
                audioManager.setRingerModeInternal(SettingsUtils.getInt(context, GameKeyFragment.KEY_GAMEKEY_ACTION_UP, GameKeyFragment.KEY_GAMEKEY_DEFAULT_ACTION_UP));
            } else if (gamekeyStatus.equals("0")) {
                audioManager.setRingerModeInternal(SettingsUtils.getInt(context, GameKeyFragment.KEY_GAMEKEY_ACTION_DOWN, GameKeyFragment.KEY_GAMEKEY_DEFAULT_ACTION_DOWN));
            } else {
                Log.e(TAG, "Unknown gameswitch status: " + gamekeyStatus);
            }
        }
    }
}
