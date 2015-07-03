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

import com.sun.javadoc.ExecutableMemberDoc;

import java.util.ArrayList;
import java.util.List;

// points from one targetMethod to 0..n testMethods which test the target method
public class AnnotationPointer {
    final ExecutableMemberDoc targetMethod;

    private List<TestTargetNew> targets = new ArrayList<TestTargetNew>();

    AnnotationPointer(ExecutableMemberDoc targetMethod) {
        this.targetMethod = targetMethod;
    }

    public void addTestTargetNew(TestTargetNew testMethodInfo) {
        /*
         * if (testMethods.contains(testMethodInfo)) { throw new
         * RuntimeException("warn: testMethod refers more than once to the
         * targetMethod, testMethod="+testMethodInfo.getMethodDoc());
         * //System.out.println("warn: testMethod refers more than once to the
         * targetMethod, testMethod="+testMethod); } else {
         */
        targets.add(testMethodInfo);
        // }
    }

    public List<TestTargetNew> getTargets() {
        return targets;
    }

    public void addProxiesFrom(AnnotationPointer ap) {
        List<TestTargetNew> t = ap.targets;
        // clone the TestTargetNew and add to it a note from which
        // target method it orignally stems
        for (TestTargetNew ttn : t) {
            TestTargetNew tnew = ttn.cloneMe("<b>(INDIRECTLY tested)</b>");
            targets.add(tnew);
        }
    }
}
