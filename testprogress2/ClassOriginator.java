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

package testprogress2;

import com.sun.javadoc.ClassDoc;

/**
 *
 */
public class ClassOriginator implements Originator {
    private final ClassDoc mClassDoc;

    private final String mComment;

    ClassOriginator(ClassDoc classDoc, String comment) {
        mClassDoc = classDoc;
        mComment = comment;
    }

    /*
     * (non-Javadoc)
     * @see testprogress.Originator#asString()
     */
    public String asString() {
        return (mComment != null ? mComment + " - " : "") + " -class- "
                + mClassDoc.qualifiedName();
    }

    public boolean isDisabled() {
        return false;
    }

    /**
     * a reference from a class is never a to be fixed
     */
    public String getToBeFixed() {
        return null;
    }

    /**
     * a reference from a class is never a broken tests
     */
    public String getBrokenTest() {
        return null;
    }

    /**
     * a reference from a class is never a failure
     */
    public String getKnownFailure() {
        return null;
    }

}
