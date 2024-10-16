/*
 * Copyright (c) 2020 The LineageOS Project
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

package co.aospa.settings.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SettingsUtils {
    public static final String PREFERENCES = "SettingsUtilsPreferences";

    /**
     * Saves given value into given preference
     * @return puts the value in place
     */
    public static boolean setEnabled(Context context, String string, boolean enabled) {
        return putInt(context, string, enabled ? 1 : 0);
    }

    /**
     * Retrieves a value from a desired string
     * @return the value
     */
    public static boolean getEnabled(Context context, String string) {
        return getInt(context, string, 0) == 1;
    }

    public static boolean getEnabled(Context context, String string, boolean def) {
        return getInt(context, string, def ? 1 : 0) == 1;
    }

    public static boolean putInt(Context context, String name, int value) {
        SharedPreferences settings = context.getSharedPreferences(PREFERENCES, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(name, value);
        return editor.commit();
    }

    public static int getInt(Context context, String name, int def) {
        SharedPreferences settings = context.getSharedPreferences(PREFERENCES, 0);
        return settings.getInt(name, def);
    }
};
