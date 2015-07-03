/*
 * Copyright (C) 2010 The Android Open Source Project
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

package android.os.cts;

import dalvik.annotation.TestTargetClass;

import android.os.Build;

import junit.framework.TestCase;

@TestTargetClass(Build.class)
public class BuildTest extends TestCase {

    private static final String ARMEABI_V7 = "armeabi-v7a";

    private static final String ARMEABI = "armeabi";

    static {
        System.loadLibrary("buildtest");
    }

    /** Tests that check the values of {@link Build#CPU_ABI} and {@link Build#CPU_ABI2}. */
    public void testCpuAbi() {
        if (isArmCpu()) {
            assertArmCpuAbiConstants();
        }
    }

    private native boolean isArmCpu();

    private void assertArmCpuAbiConstants() {
        boolean isArm7Compatible = isArm7Compatible();
        if (isArm7Compatible) {
            String message = String.format("CPU is ARM v7 compatible, so Build.CPU_ABI must be %s"
                    + " and Build.CPU_ABI2 must be %s.", ARMEABI_V7, ARMEABI);
            assertEquals(message, ARMEABI_V7, Build.CPU_ABI);
            assertEquals(message, ARMEABI, Build.CPU_ABI2);
        } else {
            String message = String.format("CPU is not ARM v7 compatible, so Build.CPU_ABI must "
                    + "be %s and Build.CPU_ABI2 must be 'unknown'.", ARMEABI);
            assertEquals(message, ARMEABI, Build.CPU_ABI);
            assertEquals(message, "unknown", Build.CPU_ABI2);
        }
    }

    private native boolean isArm7Compatible();
}
