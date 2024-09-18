/*
 * Copyright (C) 2020 The LineageOS Project
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

package co.aospa.settings.fan;

import android.content.Context;
import android.content.res.Resources;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcel;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.widget.Switch;
import android.widget.CompoundButton;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragment;
import androidx.preference.SwitchPreference;
import androidx.preference.SeekBarPreference;
import android.util.Log;

import com.android.settingslib.collapsingtoolbar.R;

import co.aospa.settings.utils.FileUtils;
import co.aospa.settings.utils.SettingsUtils;

public class FanFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener,
        SharedPreferences.OnSharedPreferenceChangeListener {
    private static String TAG = "FanFragment";
    private static final boolean DEBUG = true;

    public static final String KEY_FAN_ENABLE = "fan_control_enable";
    public static final String KEY_FAN_ENABLE_FAST_CHARGE = "fan_control_enable_fast_charge";
    public static final String KEY_FAN_MANUAL = "fan_control_manual_slider";

    public static final String ENABLE = "/sys/kernel/fan/fan_enable";
    public static final String SPEED_LEVEL = "/sys/kernel/fan/fan_speed_level";

    private static final int FAN_MIN_VALUE = 1;
    private static final int FAN_MAX_VALUE = 5;

    private SwitchPreference mFanEnableSwitch;
    private SwitchPreference mFanEnableChargeSwitch;
    private SeekBarPreference mFanManualBar;

    private String summary = null;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        int fanModeValue;
        addPreferencesFromResource(R.xml.fan);

        mFanEnableSwitch = (SwitchPreference) findPreference(KEY_FAN_ENABLE);
        mFanEnableSwitch.setChecked(SettingsUtils.getEnabled(getActivity(), KEY_FAN_ENABLE));
        mFanEnableSwitch.setOnPreferenceChangeListener(this);

        mFanEnableChargeSwitch = (SwitchPreference) findPreference(KEY_FAN_ENABLE_FAST_CHARGE);
        mFanEnableChargeSwitch.setChecked(SettingsUtils.getEnabled(getActivity(), KEY_FAN_ENABLE_FAST_CHARGE));
        mFanEnableChargeSwitch.setOnPreferenceChangeListener(this);

        mFanManualBar = (SeekBarPreference) findPreference(KEY_FAN_MANUAL);
        mFanManualBar.setValue(SettingsUtils.getInt(getActivity(), KEY_FAN_MANUAL, 1));
        mFanManualBar.setOnPreferenceChangeListener(this);
        mFanManualBar.setMin(FAN_MIN_VALUE);
        mFanManualBar.setMax(FAN_MAX_VALUE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        final String key = preference.getKey();

        if (DEBUG) Log.d(TAG, "onPreferenceChange: " + key + " => " + value);

        if (KEY_FAN_MANUAL.equals(key)) {
            int intValue = (Integer) value;
            mFanManualBar.setValue(intValue);
            setFanSpeed(getActivity(), intValue);
        } else if (KEY_FAN_ENABLE.equals(key)) {
            boolean boolValue = (Boolean) value;
            mFanEnableSwitch.setChecked(boolValue);
            setFanStatus(getActivity(), boolValue);
        } else if (KEY_FAN_ENABLE_FAST_CHARGE.equals(key)) {
            boolean boolValue = (Boolean) value;
            mFanEnableChargeSwitch.setChecked(boolValue);
            SettingsUtils.setEnabled(getActivity(), KEY_FAN_ENABLE_FAST_CHARGE, boolValue);
        }

        return true;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    }

    static private boolean mFanEnabled = false;
    public static void setFanStatus(Context context, boolean enabled) {
        if (DEBUG) Log.d(TAG, "setFanStatus: " + enabled);
        mFanEnabled = enabled;

        SettingsUtils.setEnabled(context, KEY_FAN_ENABLE, enabled);
        FileUtils.writeLine(ENABLE, enabled ? "1" : "0");
    }

    public static void setFanStatus(boolean enabled) {
        if (DEBUG) Log.d(TAG, "setFanStatus: " + enabled);
        mFanEnabled = enabled;
        FileUtils.writeLine(ENABLE, enabled ? "1" : "0");
    }

    public static boolean isFanEnabled() {
        return mFanEnabled;
    }

    public static void setFanSpeed(Context context, int level) {
        if (level < 0 || level > 5) {
            throw new IllegalArgumentException("Level must be in [0; 5], but it's " + level);
        }
        SettingsUtils.putInt(context, KEY_FAN_MANUAL, level);
        FileUtils.writeLine(SPEED_LEVEL, String.valueOf(level));
    }

    private void setFan(boolean enabled, int level) {
        setFanStatus(getActivity(), enabled);
        setFanSpeed(getActivity(), level);
    }
}
