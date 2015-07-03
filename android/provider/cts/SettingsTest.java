/*
 * Copyright (C) 2008 The Android Open Source Project
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

package android.provider.cts;

import dalvik.annotation.BrokenTest;
import dalvik.annotation.TestTargetClass;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.IContentProvider;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.Settings;
import android.test.AndroidTestCase;

@TestTargetClass(android.provider.Settings.class)
public class SettingsTest extends AndroidTestCase {
    public void testSystemTable() throws RemoteException {
        final String[] SYSTEM_PROJECTION = new String[] {
                Settings.System._ID, Settings.System.NAME, Settings.System.VALUE
        };
        final int ID_INDEX = 0;
        final int NAME_INDEX = 1;
        final int VALUE_INDEX = 2;

        String insertName = "name_insert";
        String insertValue = "value_insert";

        String updateName = "name_update";
        String updateValue = "value_update";

        // get provider
        ContentResolver cr = mContext.getContentResolver();
        IContentProvider provider = cr.acquireProvider(Settings.System.CONTENT_URI);
        Cursor cursor = null;

        try {
            // Test: insert
            ContentValues value = new ContentValues();
            value.put(Settings.System.NAME, insertName);
            value.put(Settings.System.VALUE, insertValue);

            provider.insert(Settings.System.CONTENT_URI, value);
            cursor = provider.query(Settings.System.CONTENT_URI, SYSTEM_PROJECTION,
                    Settings.System.NAME + "=\"" + insertName + "\"", null, null);
            assertNotNull(cursor);
            assertEquals(1, cursor.getCount());
            assertTrue(cursor.moveToFirst());
            assertEquals(insertName, cursor.getString(NAME_INDEX));
            assertEquals(insertValue, cursor.getString(VALUE_INDEX));
            int Id = cursor.getInt(ID_INDEX);
            cursor.close();

            // Test: update
            value.clear();
            value.put(Settings.System.NAME, updateName);
            value.put(Settings.System.VALUE, updateValue);

            provider.update(Settings.System.CONTENT_URI, value,
                    Settings.System.NAME + "=\"" + insertName + "\"", null);
            cursor = provider.query(Settings.System.CONTENT_URI, SYSTEM_PROJECTION,
                    Settings.System._ID + " = " + Id, null, null);
            assertNotNull(cursor);
            assertEquals(1, cursor.getCount());
            assertTrue(cursor.moveToFirst());
            assertEquals(updateName, cursor.getString(NAME_INDEX));
            assertEquals(updateValue, cursor.getString(VALUE_INDEX));
            cursor.close();

            // Test: delete
            provider.delete(Settings.System.CONTENT_URI,
                    Settings.System.NAME + "=\"" + updateName + "\"", null);
            cursor = provider.query(Settings.System.CONTENT_URI, SYSTEM_PROJECTION,
                    Settings.System._ID + " = " + Id, null, null);
            assertNotNull(cursor);
            assertEquals(0, cursor.getCount());
        } finally {
            // TODO should clean up more better
            if (cursor != null)
                cursor.close();
        }
    }

    public void testBluetoothDevicesTable() throws RemoteException {
        final String[] BLUETOOTH_DEVICES_PROJECTION = new String[] {
                "name", "addr", "channel", "type"
        };
        final int ID_INDEX = 0;
        final int ADDR_INDEX = 1;
        final int CHANNEL_INDEX = 2;
        final int TYPE_INDEX = 3;

        String insertName = "name_insert";
        String insertAddr = "addr_insert";

        String updateName = "name_update";
        String updateAddr = "addr_update";

        // get provider
        Uri uri = Uri.parse("content://settings/bluetooth_devices");
        ContentResolver cr = mContext.getContentResolver();
        IContentProvider provider = cr.acquireProvider(uri);
        Cursor cursor = null;

        try {
            // Test: insert
            ContentValues value = new ContentValues();
            value.put("name", insertName);
            value.put("addr", insertAddr);
            value.put("channel", 1);
            value.put("type", 2);

            provider.insert(uri, value);
            cursor = provider.query(uri, BLUETOOTH_DEVICES_PROJECTION,
                    "name=\"" + insertName + "\"", null, null);
            assertNotNull(cursor);
            assertEquals(1, cursor.getCount());
            assertTrue(cursor.moveToFirst());
            assertEquals(insertAddr, cursor.getString(ADDR_INDEX));
            assertEquals(1, cursor.getInt(CHANNEL_INDEX));
            assertEquals(2, cursor.getInt(TYPE_INDEX));
            int Id = cursor.getInt(ID_INDEX);
            cursor.close();

            // Test: update
            value.clear();
            value.put("name", updateName);
            value.put("addr", updateAddr);
            value.put("channel", 3);
            value.put("type", 4);

            provider.update(uri, value, "name=\"" + insertName + "\"", null);
            cursor = provider.query(uri, BLUETOOTH_DEVICES_PROJECTION,
                    "name=\"" + updateName + "\"", null, null);
            assertNotNull(cursor);
            assertEquals(1, cursor.getCount());
            assertTrue(cursor.moveToFirst());
            assertEquals(updateAddr, cursor.getString(ADDR_INDEX));
            assertEquals(3, cursor.getInt(CHANNEL_INDEX));
            assertEquals(4, cursor.getInt(TYPE_INDEX));
            cursor.close();

            // Test: delete
            provider.delete(uri, "name=\"" + updateName + "\"", null);
            cursor = provider.query(uri, BLUETOOTH_DEVICES_PROJECTION, "_id = " + Id, null, null);
            assertNotNull(cursor);
            assertEquals(0, cursor.getCount());
        } finally {
            // TODO should clean up more better
            if (cursor != null)
                cursor.close();
        }
    }

    @BrokenTest("Cannot access secure settings table")
    public void testSecureTable() throws RemoteException {
        final String[] SECURE_PROJECTION = new String[] {
                Settings.Secure._ID, Settings.Secure.NAME, Settings.Secure.VALUE
        };

        String insertName = "name_insert";
        String insertValue = "value_insert";

        // get provider
        ContentResolver cr = mContext.getContentResolver();
        IContentProvider provider = cr.acquireProvider(Settings.Secure.CONTENT_URI);

        // Test: insert
        ContentValues value = new ContentValues();
        value.put(Settings.Secure.NAME, insertName);
        value.put(Settings.Secure.VALUE, insertValue);

        provider.insert(Settings.Secure.CONTENT_URI, value);

        Cursor cursor = null;
        try {
            cursor = provider.query(Settings.Secure.CONTENT_URI, SECURE_PROJECTION,
                    Settings.Secure.NAME + "=\"" + insertName + "\"", null, null);
            assertNotNull(cursor);
            assertEquals(1, cursor.getCount());
        } finally {
            // TODO should clean up more better
            if (cursor != null)
                cursor.close();
        }

        try {
            provider.delete(Settings.Secure.CONTENT_URI,
                    Settings.Secure.NAME + "=\"" + insertName + "\"", null);

            cursor = provider.query(Settings.Secure.CONTENT_URI, SECURE_PROJECTION,
                    Settings.Secure.NAME + "=\"" + insertName + "\"", null, null);
            assertNotNull(cursor);
            assertEquals(0, cursor.getCount());
        } finally {
            // TODO should clean up more better
            if (cursor != null)
                cursor.close();
        }
    }
}
