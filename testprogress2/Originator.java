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

public interface Originator {

    /**
     * @return
     */
    String asString();

    /**
     * whether the originating test is disabled
     *
     * @return true if the test is disabled (starts with _test...)
     */
    boolean isDisabled();

    /**
     * indicates if the test is to be fixed (annotated with @ToBeFixed
     *
     * @return a string containing the annotation, null otherwise
     */
    String getToBeFixed();

    /**
     * indicates if the test is broken (annotated with @BrokenTest
     *
     * @return a string containing the annotation, null otherwise
     */
    String getBrokenTest();

    /**
     * indicates if the test is a known failure (runs fine on a RI, annotated
     * with @KnownFailure)
     *
     * @return a string containing the annotation, null otherwise
     */
    String getKnownFailure();

}
