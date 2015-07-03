/*
 * Copyright (C) 2009 The Android Open Source Project
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

package android.bluetooth.cts;

import android.bluetooth.BluetoothAdapter;

import junit.framework.TestCase;

/**
 * Very basic test, just of the static methods of {@link
 * BluetoothAdapter}.
 */
public class BasicAdapterTest extends TestCase {
    public void test_getDefaultAdapter() {
        /*
         * Note: If the target doesn't support Bluetooth at all, then
         * this method will return null. The assumption here is that
         * you won't bother running this test on a target that doesn't
         * purport to support Bluetooth.
         */
        assertNotNull(BluetoothAdapter.getDefaultAdapter());
    }

    public void test_checkBluetoothAddress() {
        // Can't be null.
        assertFalse(BluetoothAdapter.checkBluetoothAddress(null));

        // Must be 17 characters long.
        assertFalse(BluetoothAdapter.checkBluetoothAddress(""));
        assertFalse(BluetoothAdapter.checkBluetoothAddress("0"));
        assertFalse(BluetoothAdapter.checkBluetoothAddress("00"));
        assertFalse(BluetoothAdapter.checkBluetoothAddress("00:"));
        assertFalse(BluetoothAdapter.checkBluetoothAddress("00:0"));
        assertFalse(BluetoothAdapter.checkBluetoothAddress("00:00"));
        assertFalse(BluetoothAdapter.checkBluetoothAddress("00:00:"));
        assertFalse(BluetoothAdapter.checkBluetoothAddress("00:00:0"));
        assertFalse(BluetoothAdapter.checkBluetoothAddress("00:00:00"));
        assertFalse(BluetoothAdapter.checkBluetoothAddress("00:00:00:"));
        assertFalse(BluetoothAdapter.checkBluetoothAddress("00:00:00:0"));
        assertFalse(BluetoothAdapter.checkBluetoothAddress("00:00:00:00"));
        assertFalse(BluetoothAdapter.checkBluetoothAddress("00:00:00:00:"));
        assertFalse(BluetoothAdapter.checkBluetoothAddress("00:00:00:00:0"));
        assertFalse(BluetoothAdapter.checkBluetoothAddress("00:00:00:00:00"));
        assertFalse(BluetoothAdapter.checkBluetoothAddress("00:00:00:00:00:"));
        assertFalse(BluetoothAdapter.checkBluetoothAddress(
            "00:00:00:00:00:0"));

        // Must have colons between octets.
        assertFalse(BluetoothAdapter.checkBluetoothAddress(
            "00x00:00:00:00:00"));
        assertFalse(BluetoothAdapter.checkBluetoothAddress(
            "00:00.00:00:00:00"));
        assertFalse(BluetoothAdapter.checkBluetoothAddress(
            "00:00:00-00:00:00"));
        assertFalse(BluetoothAdapter.checkBluetoothAddress(
            "00:00:00:00900:00"));
        assertFalse(BluetoothAdapter.checkBluetoothAddress(
            "00:00:00:00:00?00"));

        // Hex letters must be uppercase.
        assertFalse(BluetoothAdapter.checkBluetoothAddress(
            "a0:00:00:00:00:00"));
        assertFalse(BluetoothAdapter.checkBluetoothAddress(
            "0b:00:00:00:00:00"));
        assertFalse(BluetoothAdapter.checkBluetoothAddress(
            "00:c0:00:00:00:00"));
        assertFalse(BluetoothAdapter.checkBluetoothAddress(
            "00:0d:00:00:00:00"));
        assertFalse(BluetoothAdapter.checkBluetoothAddress(
            "00:00:e0:00:00:00"));
        assertFalse(BluetoothAdapter.checkBluetoothAddress(
            "00:00:0f:00:00:00"));
        
        assertTrue(BluetoothAdapter.checkBluetoothAddress(
            "00:00:00:00:00:00"));
        assertTrue(BluetoothAdapter.checkBluetoothAddress(
            "12:34:56:78:9A:BC"));
        assertTrue(BluetoothAdapter.checkBluetoothAddress(
            "DE:F0:FE:DC:B8:76"));
    }
}
