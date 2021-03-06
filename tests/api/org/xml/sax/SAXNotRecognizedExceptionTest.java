/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tests.api.org.xml.sax;

import junit.framework.TestCase;

import org.xml.sax.SAXNotRecognizedException;

import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;

@TestTargetClass(SAXNotRecognizedException.class)
public class SAXNotRecognizedExceptionTest extends TestCase {

    public static final String ERR = "Houston, we have a problem";
    
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "SAXNotRecognizedException",
        args = { }
    )
    public void testSAXNotRecognizedException() {
        SAXNotRecognizedException e = new SAXNotRecognizedException();
        assertNull(e.getMessage());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "SAXNotRecognizedException",
        args = { String.class }
    )
    public void testSAXNotRecognizedException_String() {
        SAXNotRecognizedException e = new SAXNotRecognizedException(ERR);
        assertEquals(ERR, e.getMessage());
        
        e = new SAXNotRecognizedException(null);
        assertNull(e.getMessage());
    }
    
}
