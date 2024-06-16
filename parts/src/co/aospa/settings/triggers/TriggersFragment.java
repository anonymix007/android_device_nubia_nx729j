/*
 * Copyright (C) 2023 The LineageOS Project
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

package co.aospa.settings.triggers;

import android.content.res.Resources;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcel;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.widget.Switch;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragment;
import androidx.preference.SwitchPreference;

import co.aospa.settings.R;

import co.aospa.settings.utils.FileUtils;
import co.aospa.settings.utils.SettingsUtils;

public class TriggersFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String KEY_TRIGGERS_DISABLE = "triggers_disable";

    public static final String UP_TOUCH_KEY_DIFF = "/sys/class/leds/sar0/diff";
    public static final String DN_TOUCH_KEY_DIFF = "/sys/class/leds/sar1/diff";

    public static final String UP_TOUCH_KEY_PARASITIC = "/sys/class/leds/sar0/parasitic_data";
    public static final String DN_TOUCH_KEY_PARASITIC = "/sys/class/leds/sar1/parasitic_data";

    public static final String UP_TOUCH_KEY_MODE_OPERATION = "/sys/class/leds/sar0/mode_operation";
    public static final String DN_TOUCH_KEY_MODE_OPERATION = "/sys/class/leds/sar1/mode_operation";

    public static final String MODE_ENABLE = "1";
    public static final String MODE_DISABLE = "2";



    private SharedPreferences mPrefs;
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.triggers);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        final SwitchPreference triggersDisable = (SwitchPreference) findPreference(KEY_TRIGGERS_DISABLE);
        triggersDisable.setChecked(mPrefs.getBoolean(KEY_TRIGGERS_DISABLE, true));
    }

    @Override
    public void onResume() {
        super.onResume();
        mPrefs.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        mPrefs.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreference, String key) {
        if (KEY_TRIGGERS_DISABLE.equals(key)) {
            final boolean value = mPrefs.getBoolean(key, false);
            mPrefs.edit().putBoolean(KEY_TRIGGERS_DISABLE, value).apply();
            FileUtils.writeLine(UP_TOUCH_KEY_MODE_OPERATION, value ? MODE_DISABLE : MODE_ENABLE);
            FileUtils.writeLine(DN_TOUCH_KEY_MODE_OPERATION, value ? MODE_DISABLE : MODE_ENABLE);
        }
    }
}
