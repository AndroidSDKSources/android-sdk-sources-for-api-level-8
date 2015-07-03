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

import com.sun.javadoc.AnnotationDesc;
import com.sun.javadoc.AnnotationValue;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.AnnotationDesc.ElementValuePair;

import java.util.ArrayList;
import java.util.List;

/**
 * represents a list of testtargets annotations all belonging to one test method
 * / one testclass together with its processed information.
 */
public class TestMethodInformation {
    private boolean annotationExists = false;

    private List<TestTargetNew> targets = new ArrayList<TestTargetNew>();

    private String error = null;

    private Color color = Color.RED;

    public enum Level {
        TODO, PARTIAL, PARTIAL_COMPLETE, COMPLETE, ADDITIONAL, NOT_NECESSARY, NOT_FEASIBLE, SUFFICIENT
    }

    public enum Color {
        GREEN /* ready */, YELLOW /* work */, RED
        /* missing essential stuff */
    }

    public TestMethodInformation(Originator originator,
            AnnotationDesc[] annots, ClassDoc targetClass) {
        // System.out.println("looking at "+testMethodDoc);
        if (targetClass == null) {
            addError("target class annotation missing!");
            return;
        }

        for (AnnotationDesc annot : annots) {
            if (annot.annotationType().qualifiedName().equals(
                    "dalvik.annotation.TestTargets")) {
                // multi target case
                annotationExists = true;
                ElementValuePair[] pairs = annot.elementValues();
                if (pairs.length != 1
                        && !pairs[0].element().qualifiedName().equals(
                                "dalvik.annotation.TestTargets.value")) {
                    throw new RuntimeException("TestTargets has mismatched "
                            + "attributes");
                }
                AnnotationValue[] targets = (AnnotationValue[])pairs[0].value()
                        .value();
                for (AnnotationValue ttn : targets) {
                    // the test targets must be annotations themselves
                    AnnotationDesc ttnd = (AnnotationDesc)ttn.value();
                    handleTestTargetNew(originator, ttnd, targetClass);
                }
            } else if (annot.annotationType().qualifiedName().equals(
                    "dalvik.annotation.TestTargetNew")) {
                // singular case
                annotationExists = true;
                handleTestTargetNew(originator, annot, targetClass);
            } // else some other annotation - ignore
        }

        boolean targetsCorrect = true;
        for (TestTargetNew ttn : targets) {
            targetsCorrect &= (ttn.getTargetMethod() != null || ttn
                    .getTargetClass() != null);
        }

        // calculate color of test method
        if (annotationExists) {
            if (targetsCorrect) {
                color = Color.GREEN;
            } // else incorrect targets
        } else {
            addError("no annotation!");
        }
    }

    private void handleTestTargetNew(Originator originator, AnnotationDesc ttn,
            ClassDoc targetClass) {
        TestTargetNew testTarget = new TestTargetNew(originator, ttn,
                targetClass);
        if (testTarget.isHavingProblems()) {
            // add to overall message
            addError(testTarget.getNotes());
        }
        targets.add(testTarget);
    }

    private void addError(String err) {
        if (error == null)
            error = "";
        error += err + " ; ";
    }

    /**
     * @return the error
     */
    public String getError() {
        return error;
    }

    public List<TestTargetNew> getTargets() {
        return targets;
    }

    public Color getColor() {
        return color;
    }

}
