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

package com.android.cts.instrumentationdiffcertapp;

import junit.framework.TestCase;

/**
 * Test that is expected not to run
 */
public class InstrumentationFailToRunTest extends TestCase {

    /**
     * Test method that is expected not to run.
     */
    public void testInstrumentationNotAllowed() {
        fail("instrumentating app with different cert should fail");
    }
}
