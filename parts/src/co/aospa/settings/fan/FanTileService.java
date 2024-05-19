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

import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import co.aospa.settings.utils.FileUtils;
import co.aospa.settings.utils.SettingsUtils;

import co.aospa.settings.fan.FanFragment;

public class FanTileService extends TileService {
    private static final String TAG = "FanTileService";
    private static final boolean DEBUG = true;

    public void onStartListening() {
        Tile tile = getQsTile();
        if (FanFragment.isFanEnabled()) {
            tile.setState(Tile.STATE_ACTIVE);
        } else {
            tile.setState(Tile.STATE_INACTIVE);
        }
        tile.updateTile();
        super.onStartListening();
    }

    @Override
    public void onClick() {
        Tile tile = getQsTile();
        if (FanFragment.isFanEnabled()) {
            FanFragment.setFanStatus(this, false);
            tile.setState(Tile.STATE_INACTIVE);
        } else {
            FanFragment.setFanStatus(this, true);
            tile.setState(Tile.STATE_ACTIVE);
        }
        tile.updateTile();
        super.onClick();
    }
}
