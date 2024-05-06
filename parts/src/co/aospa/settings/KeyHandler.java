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

package co.aospa.settings;

import android.content.Context;
import android.hardware.input.InputManager;
import android.media.AudioManager;
import android.os.SystemProperties;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.util.Log;

import com.android.internal.os.DeviceKeyHandler;

import co.aospa.settings.gamekey.GameKeyFragment;
import co.aospa.settings.utils.SettingsUtils;

public class KeyHandler implements DeviceKeyHandler {
    private static final String TAG = KeyHandler.class.getSimpleName();

    public static final String GAMEKEY_STATUS_PATH = "/sys/devices/platform/soc/soc:gpio_keys_nubia/GamekeyStatus";

    private final Context mContext;
    private final AudioManager mAudioManager;
    private final InputManager mInputManager;
    private final Vibrator mVibrator;

    public KeyHandler(Context context) {
        Log.e(TAG, "Creating new gameswitch key handler");
        mContext = context;

        mAudioManager = mContext.getSystemService(AudioManager.class);
        mInputManager = mContext.getSystemService(InputManager.class);
        mVibrator = mContext.getSystemService(Vibrator.class);
    }

    public KeyEvent handleKeyEvent(KeyEvent event) {
        if (!mInputManager.getInputDevice(event.getDeviceId()).getName().equals("gpio-keys_nubia")) {
            Log.d(TAG, "Got event " + event + " event from " + mInputManager.getInputDevice(event.getDeviceId()));
            return event;
        }

        if (!SystemProperties.getBoolean(GameKeyFragment.SYSPROP_GAMEKEY_ENABLE, true)) {
            Log.d(TAG, "Got gamekey event " + event + ", but it's disabled");
            return event;
        }

        if (event.getKeyCode() != KeyEvent.KEYCODE_F8) {
            Log.e(TAG, "Got unknown event: " + event);
            return event;
        }

        Log.e(TAG, "Got gameswitch event: " + event);

        switch(event.getAction()) {
            case KeyEvent.ACTION_DOWN:
                mAudioManager.setRingerModeInternal(SystemProperties.getInt(GameKeyFragment.SYSPROP_GAMEKEY_ACTION_DOWN, GameKeyFragment.KEY_GAMEKEY_DEFAULT_ACTION_DOWN));
                break;
            case KeyEvent.ACTION_UP:
                mAudioManager.setRingerModeInternal(SystemProperties.getInt(GameKeyFragment.SYSPROP_GAMEKEY_ACTION_UP, GameKeyFragment.KEY_GAMEKEY_DEFAULT_ACTION_UP));
                break;
            default:
                return event;
        }

        doHapticFeedback();

        return null;
    }

    private void doHapticFeedback() {
        if (mVibrator != null && mVibrator.hasVibrator()) {
            mVibrator.vibrate(VibrationEffect.createOneShot(50,
                    VibrationEffect.DEFAULT_AMPLITUDE));
        }
    }
}

