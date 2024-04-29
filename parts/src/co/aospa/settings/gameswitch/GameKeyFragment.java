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

import android.content.res.Resources;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcel;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.widget.Switch;
import android.media.AudioManager;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragment;
import androidx.preference.DropDownPreference;

import com.android.settingslib.widget.MainSwitchPreference;

import co.aospa.settings.R;

import co.aospa.settings.utils.FileUtils;
import co.aospa.settings.utils.SettingsUtils;

import android.util.Log;

public class GameKeyFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = "GameKeyFragment";

    public static final String KEY_GAMEKEY_ENABLE = "gamekey_enable";
    public static final String KEY_GAMEKEY_ACTION_UP = "gamekey_action_up";
    public static final String KEY_GAMEKEY_ACTION_DOWN = "gamekey_action_down";

    public static final int KEY_GAMEKEY_DEFAULT_ACTION_DOWN = AudioManager.RINGER_MODE_SILENT;
    public static final int KEY_GAMEKEY_DEFAULT_ACTION_UP = AudioManager.RINGER_MODE_NORMAL;

    private SharedPreferences mPrefs;
    private MainSwitchPreference mGamekeyEnable;
    private DropDownPreference mUpAction;
    private DropDownPreference mDownAction;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.gamekey);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        mGamekeyEnable = (MainSwitchPreference) findPreference(KEY_GAMEKEY_ENABLE);
        mGamekeyEnable.setChecked(mPrefs.getBoolean(KEY_GAMEKEY_ENABLE, true));

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

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if (KEY_GAMEKEY_ACTION_DOWN.equals(key)) {
            int downAction = Integer.parseInt(mDownAction.getValue());
            SettingsUtils.putInt(getActivity(), KEY_GAMEKEY_ACTION_DOWN, downAction);
            mDownAction.setSummary(getActionSummaryFromValue(downAction));
        } else if (KEY_GAMEKEY_ACTION_UP.equals(key)) {
            int upAction = Integer.parseInt(mUpAction.getValue());
            SettingsUtils.putInt(getActivity(), KEY_GAMEKEY_ACTION_UP, upAction);
            mUpAction.setSummary(getActionSummaryFromValue(upAction));
        }
    }
}
