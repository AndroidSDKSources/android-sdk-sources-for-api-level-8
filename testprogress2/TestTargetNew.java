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
import com.sun.javadoc.ExecutableMemberDoc;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.Parameter;
import com.sun.javadoc.ParameterizedType;
import com.sun.javadoc.Type;
import com.sun.javadoc.TypeVariable;
import com.sun.javadoc.AnnotationDesc.ElementValuePair;

import testprogress2.TestMethodInformation.Level;

/**
 * holder for a TestTargetNew annotation
 */
public class TestTargetNew {
    private final Originator originator;

    private Level level = null;

    private String notes = null;

    /*
     * method or constructor of target class
     */
    private ExecutableMemberDoc targetMethod = null;

    /*
     * only set if the target points -only- to a class, not to a method. e.g for
     * special "!..." targets
     */
    private ClassDoc targetClass = null;

    /*
     * read from annotation, e.g. foobar(java.lang.String)
     */
    private String readMethodSignature = null;

    /*
     * e.g. foobar
     */
    private String readMethodName = null;

    /*
     * read from annotation
     */
    private ClassDoc readTargetClass = null;

    private boolean havingProblems = false;

    private TestTargetNew(Originator originator) {
        this.originator = originator;
    }

    /**
     * @param originator the origin (class or method)
     * @param ttn the annotation (testtargetnew)
     * @param classLevelTargetClass the default target class as given in the
     *            testtargetclass annotation
     */
    public TestTargetNew(Originator originator, AnnotationDesc ttn,
            ClassDoc classLevelTargetClass) {
        this.originator = originator;
        parseTargetClassAndMethodSignature(ttn, classLevelTargetClass);
        // post: readMethod, readMethodSignature and readTargetClass are now set

        // test for artificial method targets
        if (readMethodName.startsWith("!")) {
            targetMethod = null;
            targetClass = readTargetClass;
            // level = Level.ADDITIONAL;
            // notes already set
            notes = "target: " + readMethodName
                    + (notes != null ? ", " + "notes: " + notes : "");

        } else if (level == Level.TODO) {
            notes = "TODO :" + notes;
            havingProblems = true;
        } else {
            // prepare method target:
            // if the signature contains a "." then the prefix is used as a
            // reference
            // to an inner class. This is an alternative to using the clazz
            // attribute in cases where the class is an inner protected class,
            // because then the inner class is not visible for the compiler at
            // the
            // place of the annotation.
            // e.g. clazz = Certificate.CertificateRep.class does not work,
            // so we use clazz = Certificate.class (enclosing class), and method
            // "Certificate.CertificateRep.<methodHere>", e.g.
            // "CertificateRep.CertificateRep"
            // to denote the constructor of the inner protected class
            // CertificateRep
            // within Certificate
            int dotPos = readMethodName.lastIndexOf('.');
            if (dotPos != -1) {
                String prefixClassName = readMethodName.substring(0, dotPos);
                readMethodName = readMethodName.substring(dotPos + 1);
                ClassDoc[] iCs = readTargetClass.innerClasses();
                for (ClassDoc iC : iCs) {
                    if (iC.name().equals(prefixClassName)) {
                        readTargetClass = iC;
                        break;
                    }
                }
            }

            String methodAndSig = readMethodName + readMethodSignature;
            ExecutableMemberDoc tmeth = findMethodSignatureIn(methodAndSig,
                    readTargetClass);
            // we need this double test for the note below
            if (tmeth == null) {
                // a) wrong signature or
                // b) a testMethod in a superclass or superinterface, ok also
                tmeth = findTargetMethodInSelfAndSupers(methodAndSig,
                        readTargetClass);
                if (tmeth != null) {
                    if (notes == null)
                        notes = "";
                    notes += "- targetmethod (" + tmeth + ") was found in a "
                            + "superclass/superinterface of the target<br>";
                }
            }
            if (tmeth != null) {
                // found
                targetMethod = tmeth;
            } else {
                havingProblems = true;
                notes = "From " + originator.asString()
                        + " -> could not resolve " + "targetMethod for class "
                        + readTargetClass + ", " + "annotation was:" + ttn
                        + ", testMethodSig " + "= " + methodAndSig + "<br>";
                System.err.println(">>> warning: " + notes);
            }
        }
    }

    private ExecutableMemberDoc findMethodSignatureIn(String sig,
            ClassDoc targetClass) {
        ExecutableMemberDoc targetMethod = null;
        // find the matching method in the target class, check all methods
        for (ExecutableMemberDoc mdoc : targetClass.methods()) {
            if (equalsSignature(mdoc, sig)) {
                return mdoc;
            }
        }
        // check constructors, too
        for (ExecutableMemberDoc mdoc : targetClass.constructors()) {
            if (equalsSignature(mdoc, sig)) {
                return mdoc;
            }
        }
        return null;
    }

    private ExecutableMemberDoc findTargetMethodInSelfAndSupers(String sig,
            ClassDoc targetClass) {
        ExecutableMemberDoc mem = findMethodSignatureIn(sig, targetClass);
        if (mem != null) {
            return mem;
        }

        // else visit parent class or parent interface(s)
        ClassDoc[] ifs = targetClass.interfaces();
        for (int i = 0; i < ifs.length; i++) {
            ClassDoc iface = ifs[i];
            mem = findTargetMethodInSelfAndSupers(sig, iface);
            if (mem != null) {
                return mem;
            }
        }

        ClassDoc superclass = targetClass.superclass();
        if (superclass != null) {
            mem = findTargetMethodInSelfAndSupers(sig, superclass);
            if (mem != null) {
                return mem;
            }
        }
        return null;
    }

    private void parseTargetClassAndMethodSignature(AnnotationDesc targetAnnot,
            ClassDoc targetClass) {
        ElementValuePair[] pairs = targetAnnot.elementValues();
        String methodName = null;
        String args = "";
        for (ElementValuePair kval : pairs) {
            if (kval.element().name().equals("method")) {
                methodName = (String)kval.value().value();
            } else if (kval.element().name().equals("clazz")) {
                // optional: a different target class than the test-class-level
                // default.
                Object obj = kval.value().value();
                if (obj instanceof ClassDoc) {
                    targetClass = (ClassDoc)obj;
                } else if (obj instanceof ParameterizedType) {
                    targetClass = ((ParameterizedType)obj).asClassDoc();
                } else {
                    throw new RuntimeException("annotation elem value is of "
                            + "type " + obj.getClass().getName() + " target "
                            + "annotation = " + targetAnnot);
                }
            } else if (kval.element().name().equals("args")) {
                AnnotationValue[] vals = (AnnotationValue[])kval.value()
                        .value();
                for (int i = 0; i < vals.length; i++) {
                    AnnotationValue arg = vals[i];
                    String argV;
                    // TODO: we should be able to use Type.asClassDoc() here
                    if (arg.value() instanceof ClassDoc) {
                        ClassDoc cd = (ClassDoc)arg.value();
                        argV = cd.qualifiedName();
                    } else { // primitive type or array type
                        // is there a nicer way to do this?
                        argV = arg.toString();
                    }
                    // strip .class out of args since signature does not contain
                    // those
                    if (argV.endsWith(".class")) {
                        argV = argV.substring(0, argV.length() - 6);
                    }
                    args += (i > 0 ? "," : "") + argV;
                }
            } else if (kval.element().name().equals("level")) {
                AnnotationValue lev = kval.value();
                FieldDoc fd = (FieldDoc)lev.value();
                String slevel = fd.name();

                try {
                    level = Enum.valueOf(Level.class, slevel);
                } catch (IllegalArgumentException iae) {
                    throw new RuntimeException("COMPILE ERROR!!! enum "
                            + slevel + " used in targetMethod for class "
                            + "\"+targetClass+\", "
                            + "annotation was:\"+targetAnnot+\", "
                            + "testMethod = \"+methodDoc.toString()");
                }
            } else if (kval.element().name().equals("notes")) {
                notes = (String)kval.value().value();
                if (notes.equals("")) {
                    notes = null;
                }
            }
        }

        // String refSig = methodName + "(" + args + ")";
        // both methodName and methodArgs != null because of Annotation
        // definition
        this.readTargetClass = targetClass;
        this.readMethodSignature = "(" + args + ")";
        this.readMethodName = methodName;
    }

    private boolean equalsSignature(ExecutableMemberDoc mdoc,
            String refSignature) {
        Parameter[] params = mdoc.parameters();
        String targs = "";
        for (int i = 0; i < params.length; i++) {
            Parameter parameter = params[i];
            // check for generic type types
            Type ptype = parameter.type();

            TypeVariable typeVar = ptype.asTypeVariable();
            String ptname;
            if (typeVar != null) {
                ptname = "java.lang.Object"; // the default fallback
                Type[] bounds = typeVar.bounds();
                if (bounds.length > 0) {
                    ClassDoc typeClass = bounds[0].asClassDoc();
                    ptname = typeClass.qualifiedName();
                }
                String dim = ptype.dimension();
                if (dim != null && dim.length() > 0) {
                    ptname += dim;
                }
            } else {
                // regular var
                // ptname = parameter.type().qualifiedTypeName();
                ptname = parameter.type().toString();

                // System.out.println("quali:"+ptname);
                // ptname = parameter.typeName();
                // omit type signature
                ptname = ptname.replaceAll("<.*>", "");
            }
            targs += (i > 0 ? "," : "") + ptname;
        }

        String methodName = mdoc.name();
        int lastDot = methodName.lastIndexOf('.');
        if (lastDot != -1) {
            // we have a inner class constructor
            // shrink the name to just name the constructor
            methodName = methodName.substring(lastDot + 1);
        }

        String testSig = methodName + "(" + targs + ")";

        // return testSig.equals(refSignature);
        if (testSig.equals(refSignature)) {
            // System.out.println("match!!!: ref = "+refSignature+",
            // test = "+testSig);
            return true;
        } else {
            // System.out.println("no match: ref = "+refSignature+",
            // test = "+testSig);
            return false;
        }
    }

    public Level getLevel() {
        return level;
    }

    public boolean isHavingProblems() {
        return havingProblems;
    }

    public Originator getOriginator() {
        return originator;
    }

    TestTargetNew cloneMe(String extraNote) {
        TestTargetNew anew = new TestTargetNew(this.originator);
        anew.level = this.level;
        anew.notes = this.notes;
        anew.targetMethod = this.targetMethod;
        anew.readMethodSignature = this.readMethodSignature;
        anew.readTargetClass = this.readTargetClass;

        // mark indirectly tested method always as green, independent
        // of the original status (to better estimate workload)
        // anew.level = Level.COMPLETE;
        anew.notes = extraNote + (notes != null ? ", " + notes : "");
        return anew;
    }

    public ExecutableMemberDoc getTargetMethod() {
        return targetMethod;
    }

    /**
     * @return the class of the testtargetnew which method starts with "!", null
     *         otherwise
     */
    public ClassDoc getTargetClass() {
        return targetClass;
    }

    public String getNotes() {
        return notes;
    }
}
