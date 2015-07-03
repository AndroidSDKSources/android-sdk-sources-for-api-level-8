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
package spechelper;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeParameter;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 */
public class MethodSelector {

    public String obtainReplacement(String buffer) {
        IMethod method = selectMethod();
        // if user did cancel the selection
        if (method == null) {
            return null;
        }

        // see if we are already in a annotation:
        // if yes -> only dump the testtarget annotation, not the complete
        // TestInfo
        // (could not easily find this out with CompilationUnit, since inserting
        // a :
        // broke the AST - maybe use WorkingCopy and so on,
        // but for now: do it with simple String analysis
        boolean shortOnly = false;
        int annotPos = buffer.lastIndexOf("@TestInfo");
        // the latest annotation - count "(" ")" pairs - if not the same count
        // we assume to be in the annotation (H: code compiles fine)
        if (annotPos != -1) {
            String sub = buffer.substring(annotPos);
            // only consider the latest 6 lines for the annotation to occur
            // (6 = range within which the annotation @TestTarget
            // must occur, but out of range to reach the annotation from the
            // previous method - ah i'd prefer working with compilationUnit...
            String[] lines = sub.split("\n");
            for (int i = lines.length - 6; i < lines.length; i++) {
                String line = lines[i];
                if (line.contains("@TestTarget")) {
                    shortOnly = true;
                }
            }
        }

        return generateAnnotation(shortOnly, method);
    }


    private String generateAnnotation(boolean shortOnly, IMethod method) {
        String[] ptypes = method.getParameterTypes();
        String param = "";
        for (int i = 0; i < ptypes.length; i++) {
            String ptype = ptypes[i];
            String sig = Signature.toString(ptype);
            // kind of a hack: convert all Generic Type args to Object, or to
            // its bound Type
            if (sig.length() == 1) {
                ITypeParameter tps = method.getTypeParameter(sig);
                sig = "Object";

                if (tps != null && tps.exists()) {
                    try {
                        String[] bounds = tps.getBounds();
                        if (bounds.length > 0) {
                            sig = bounds[0];
                        }
                    } catch (JavaModelException e) {
                        e.printStackTrace();
                    }

                }
            }
            // omit type signature
            sig = sig.replaceAll("<.*>", "");
            param += (i > 0 ? ", " : "") + sig + ".class";
        }
        String IND = "    ";

        String targ = "@TestTarget(\n" + IND + "      methodName = \""
                + method.getElementName() + "\",\n" + IND
                + "      methodArgs = {" + param + "}\n" + IND + "    )\n";

        String s;
        if (shortOnly) {
            s = targ;
        } else {

            s = "@TestInfo(\n" + IND + "  status = TestStatus.TBR,\n" + IND
                    + "  notes = \"\",\n" + IND + "  targets = {\n" + IND
                    + "    " + targ + IND + "})";
        }
        return s;
    }

    private IMethod selectMethod() {
        IEditorPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getActivePage().getActiveEditor();
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getShell();
        IEditorInput ei = part.getEditorInput();
        final ICompilationUnit cu = JavaPlugin.getDefault()
                .getWorkingCopyManager().getWorkingCopy(ei);
        // cu != null since we register only for java/javadoc completion
        // proposals
        ASTParser parser = ASTParser.newParser(AST.JLS3);
        parser.setSource(cu);
        parser.setResolveBindings(true);
        CompilationUnit unit = (CompilationUnit) parser.createAST(null);

        class MHolder {
            IMethod method;
        }
        final MHolder mholder = new MHolder();

        class FHolder {
            boolean foundClassAnnotation;
        }
        final FHolder fholder = new FHolder();

        unit.accept(new ASTVisitor() {
            public boolean visit(SingleMemberAnnotation node) {
                String name = node.getTypeName().getFullyQualifiedName();
                if (!name.equals("TestTargetClass")) {
                    return false;
                }
                fholder.foundClassAnnotation = true;
                Expression targetClassE = node.getValue();
                ITypeBinding ty = targetClassE.resolveTypeBinding();
                if (ty == null) {
                    return false;
                }
                ITypeBinding[] classTypes = ty.getTypeArguments();
                if (classTypes.length > 0) {
                    ITypeBinding tp = classTypes[0];
                    String qname = tp.getQualifiedName();
                    System.out.println("qname:" + qname);
                    IJavaProject myProject = cu.getJavaProject();
                    try {
                        IType myType = myProject.findType(qname);
                        if (myType != null) {
                            Shell parent = PlatformUI.getWorkbench()
                                    .getActiveWorkbenchWindow().getShell();
                            ElementListSelectionDialog dialog = new ElementListSelectionDialog(
                                    parent,
                                    new JavaElementLabelProvider(
                                            JavaElementLabelProvider.SHOW_PARAMETERS
                                                    | JavaElementLabelProvider.SHOW_OVERLAY_ICONS
                                                    | JavaElementLabelProvider.SHOW_RETURN_TYPE));
                            // restrict to public/protected methods only
                            IMethod[] allMeth = myType.getMethods();
                            List<IMethod> pubproMethods = new ArrayList<IMethod>();
                            for (int i = 0; i < allMeth.length; i++) {
                                IMethod method = allMeth[i];
                                if ((method.getFlags() & (Flags.AccPublic | Flags.AccProtected)) != 0) {
                                    pubproMethods.add(method);
                                }
                            }
                            IMethod[] res = pubproMethods
                                    .toArray(new IMethod[pubproMethods.size()]);
                            dialog.setIgnoreCase(true);
                            dialog.setBlockOnOpen(true);
                            dialog.setElements(res);//
                            dialog.setFilter("");
                            dialog.setTitle(qname);
                            if (dialog.open() != IDialogConstants.CANCEL_ID) {
                                Object[] types = dialog.getResult();
                                System.out.println("selected:" + types[0]);
                                IMethod method = (IMethod) types[0];
                                mholder.method = method;

                            } else {
                                // System.out.println("cancelled!!");
                            }
                        }
                    } catch (JavaModelException e) {
                        e.printStackTrace();
                    }
                }
                return true;
            }
        });
        if (!fholder.foundClassAnnotation) {
            MessageDialog.openInformation(shell, "Class Annotation missing",
                    "@TestTargetClass(...) is missing");
            return null;
        }
        return mholder.method;
    }
}
