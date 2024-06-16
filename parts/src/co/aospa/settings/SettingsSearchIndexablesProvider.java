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

package co.aospa.settings;

import java.util.List;
import java.util.Arrays;

import android.util.Log;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.provider.SearchIndexableResource;
import android.provider.SearchIndexablesProvider;
import android.provider.SearchIndexablesContract.XmlResource;
import static android.provider.SearchIndexablesContract.INDEXABLES_XML_RES_COLUMNS;
import static android.provider.SearchIndexablesContract.INDEXABLES_RAW_COLUMNS;
import static android.provider.SearchIndexablesContract.NON_INDEXABLES_KEYS_COLUMNS;

import co.aospa.settings.fan.FanActivity;
import co.aospa.settings.gamekey.GameKeyActivity;
import co.aospa.settings.triggers.TriggersActivity;

public class SettingsSearchIndexablesProvider extends SearchIndexablesProvider {
    private static String TAG = "NubiaSettingsSearchIndexablesProvider";

    @Override
    public boolean onCreate() {
        return true;
    }

    private static final List<SearchIndexableResource> SEARCHABLE_RESOURCES = Arrays.asList(
        new SearchIndexableResource(0, R.xml.fan, FanActivity.class.getName(), 0),
        new SearchIndexableResource(0, R.xml.nubiaparts,  NubiaParts.class.getName(), 0),
        new SearchIndexableResource(0, R.xml.gamekey, GameKeyActivity.class.getName(), 0),
        new SearchIndexableResource(0, R.xml.triggers, TriggersActivity.class.getName(), 0)
    );

    @Override
    public Cursor queryXmlResources(String[] projection) {
        final Context context = getContext();
        final MatrixCursor cursor = new MatrixCursor(INDEXABLES_XML_RES_COLUMNS);

        for (SearchIndexableResource indexableResource : SEARCHABLE_RESOURCES) {
            Log.w(TAG, "Add row for activity " + indexableResource.className);
            cursor.newRow()
                .add(XmlResource.COLUMN_XML_RESID, indexableResource.xmlResId)
                .add(XmlResource.COLUMN_CLASS_NAME, indexableResource.className)
                .add(XmlResource.COLUMN_INTENT_TARGET_PACKAGE, context.getPackageName())
                .add(XmlResource.COLUMN_INTENT_TARGET_CLASS, indexableResource.className);
        }
        return cursor;
    }

    @Override
    public Cursor queryRawData(String[] projection) {
        return new MatrixCursor(INDEXABLES_RAW_COLUMNS);
    }

    @Override
    public Cursor queryNonIndexableKeys(String[] projection) {
        return new MatrixCursor(NON_INDEXABLES_KEYS_COLUMNS);
    }
}