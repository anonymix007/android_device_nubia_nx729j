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

package co.aospa.settings.gamekey;

import android.content.Context;
import android.content.res.Resources;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.widget.Switch;
import android.media.AudioManager;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragment;
import androidx.preference.DropDownPreference;

import com.android.settingslib.widget.MainSwitchPreference;
import com.android.settingslib.widget.OnMainSwitchChangeListener;


import co.aospa.settings.R;

import co.aospa.settings.utils.FileUtils;
import co.aospa.settings.utils.SettingsUtils;

import android.util.Log;

public class GameKeyFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener, OnMainSwitchChangeListener {
    private static final String TAG = "GameKeyFragment";

    private static final String GAMEKEY_SYSPROP_PREFIX = "persist.gamekey.settings.";

    public static final String KEY_GAMEKEY_ENABLE = "gamekey_enable";
    public static final String KEY_GAMEKEY_ACTION_UP = "gamekey_action_up";
    public static final String KEY_GAMEKEY_ACTION_DOWN = "gamekey_action_down";

    public static final String SYSPROP_GAMEKEY_ENABLE = GAMEKEY_SYSPROP_PREFIX + KEY_GAMEKEY_ENABLE;
    public static final String SYSPROP_GAMEKEY_ACTION_UP = GAMEKEY_SYSPROP_PREFIX + KEY_GAMEKEY_ACTION_UP;
    public static final String SYSPROP_GAMEKEY_ACTION_DOWN = GAMEKEY_SYSPROP_PREFIX + KEY_GAMEKEY_ACTION_DOWN;



    public static final int KEY_GAMEKEY_DEFAULT_ACTION_DOWN = AudioManager.RINGER_MODE_NORMAL;
    public static final int KEY_GAMEKEY_DEFAULT_ACTION_UP = AudioManager.RINGER_MODE_SILENT;

    private SharedPreferences mPrefs;
    private MainSwitchPreference mGamekeyEnable;
    private DropDownPreference mUpAction;
    private DropDownPreference mDownAction;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.gamekey);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        mGamekeyEnable = (MainSwitchPreference) findPreference(KEY_GAMEKEY_ENABLE);
        mGamekeyEnable.setChecked(SettingsUtils.getInt(getActivity(), KEY_GAMEKEY_ENABLE, 1) == 1);
        mGamekeyEnable.addOnSwitchChangeListener(this);

        mDownAction = (DropDownPreference) findPreference(KEY_GAMEKEY_ACTION_DOWN);
        mUpAction = (DropDownPreference) findPreference(KEY_GAMEKEY_ACTION_UP);

        int downAction = SettingsUtils.getInt(getActivity(), KEY_GAMEKEY_ACTION_DOWN, KEY_GAMEKEY_DEFAULT_ACTION_DOWN);
        int upAction = SettingsUtils.getInt(getActivity(), KEY_GAMEKEY_ACTION_UP, KEY_GAMEKEY_DEFAULT_ACTION_UP);

        mDownAction.setValue(String.valueOf(downAction));
        mUpAction.setValue(String.valueOf(upAction));

        mDownAction.setSummary(getActionSummaryFromValue(downAction));
        mUpAction.setSummary(getActionSummaryFromValue(upAction));
    }

    private String getActionSummaryFromValue(int action) {
        switch (action) {
            case AudioManager.RINGER_MODE_SILENT: return getResources().getString(R.string.gamekey_action_silent);
            case AudioManager.RINGER_MODE_NORMAL: return getResources().getString(R.string.gamekey_action_ring);
            case AudioManager.RINGER_MODE_VIBRATE: return getResources().getString(R.string.gamekey_action_vibrate);
            default: return null;
        }
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

    public static void loadGameKeySysPropsFromPrefs(Context context) {
        saveGameKeySysProps(SettingsUtils.getBoolean(context, KEY_GAMEKEY_ENABLE, true),
                            SettingsUtils.getInt(context, KEY_GAMEKEY_ACTION_DOWN, KEY_GAMEKEY_DEFAULT_ACTION_DOWN),
                            SettingsUtils.getInt(context, KEY_GAMEKEY_ACTION_UP, KEY_GAMEKEY_DEFAULT_ACTION_UP));
    }

    public static void saveGameKeySysProps(boolean enable, int downAction, int upAction) {
        Log.d(TAG, "saveGameKeySysProps: enable " + enable + ", down " + downAction + ", up " + upAction);
        SystemProperties.set(SYSPROP_GAMEKEY_ENABLE, enable ? "true" : "false");
        SystemProperties.set(SYSPROP_GAMEKEY_ACTION_DOWN, String.valueOf(downAction));
        SystemProperties.set(SYSPROP_GAMEKEY_ACTION_UP, String.valueOf(upAction));
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d(TAG, "onSharedPreferenceChanged: " + key + ", " + sharedPreferences);
        if (KEY_GAMEKEY_ACTION_DOWN.equals(key)) {
            int downAction = Integer.parseInt(mDownAction.getValue());
            SettingsUtils.putInt(getActivity(), KEY_GAMEKEY_ACTION_DOWN, downAction);
            SystemProperties.set(SYSPROP_GAMEKEY_ACTION_DOWN, String.valueOf(downAction));
            mDownAction.setSummary(getActionSummaryFromValue(downAction));
        } else if (KEY_GAMEKEY_ACTION_UP.equals(key)) {
            int upAction = Integer.parseInt(mUpAction.getValue());
            SettingsUtils.putInt(getActivity(), KEY_GAMEKEY_ACTION_UP, upAction);
            SystemProperties.set(SYSPROP_GAMEKEY_ACTION_UP, String.valueOf(upAction));
            mUpAction.setSummary(getActionSummaryFromValue(upAction));
        }
    }

    public void onSwitchChanged(Switch switchView, boolean isChecked) {
        SettingsUtils.setEnabled(getActivity(), KEY_GAMEKEY_ENABLE, isChecked);
        SystemProperties.set(SYSPROP_GAMEKEY_ENABLE, isChecked ? "true" : "false");
    }
}
