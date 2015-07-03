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

import dalvik.annotation.TestTargetClass;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;

/**
 * <p>
 * a plugin to auto-insert the following annotation constructs: 
 * TestInfo.java, TestStatus.java, TestTarget.java, and TestTargetClass.java
 * under 
 * /android/device/dalvik/libcore/dalvik/src/main/java/dalvik/annotation/
 * <p>
 * usage:<br>
 * - install export/plugins/spechelper_1.0.0.jar into your eclipse/plugin folder.<br>
 * - restart eclipse<br>
 * - open a java file<br>
 * - insert the TestTargetClass annotation above the class declaration, e.g.
 *   <code>@TestTargetClass(Pattern.class)</code><br>
 * - insert a ":" one line above the signature of a method to be annotated,  
 *   and press ctrl-space for eclipse autocompletion. a popup appears which
 *   lists all target methods. choose one, and the annotation will be filled in
 *   at the cursor position.<br>
 * <p>  
 *   to annotate more than one target method, simply add a comma after the
 *   first TestTarget, press enter and insert a ":", press ctrl-space again.
 *   
 * <p>
 *  a sample:  
 *   
<pre>
package org.apache.harmony.tests.java.util.regex;

import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestInfo;
import dalvik.annotation.TestTarget;
import dalvik.annotation.TestStatus;

import junit.framework.TestCase;

import java.util.regex.Pattern;

@TestTargetClass(Pattern.class)

public class PatternTest extends TestCase {
    
    // add ":", press ctrl-space here to let the eclipse plugin generate 
    // the next few lines
    @TestInfo(
      status = TestStatus.TBR,
      notes = "",
      targets = {
        @TestTarget(
          methodName = "compile",
          methodArgs = {String.class}
        )
    })
    public void foo() {
        //
    }

    @TestInfo(
      status = TestStatus.TBR,
      notes = "",
      targets = {
        @TestTarget(
          methodName = "compile",
          methodArgs = {String.class}
        ),
        // add ":", press ctrl-space here to insert another TestTarget
    })
    public void bar() {
        //
    }
    
    @TestInfo(
      status = TestStatus.TBR,
      notes = "",
      targets = {
        @TestTarget(
          methodName = "compile",
          methodArgs = {String.class}
        ),
        @TestTarget(
          methodName = "split",
          methodArgs = {CharSequence.class, int.class}
        )

    })
    public void foobarsample() {
        //
    }
    
}
</pre>
 *   
 *   
 *
 */
public class SimpleComputer implements IJavaCompletionProposalComputer {

    public List<ICompletionProposal> computeCompletionProposals(
            ContentAssistInvocationContext context, IProgressMonitor monitor) {
        List<ICompletionProposal> ret = new Vector<ICompletionProposal>();
        try {
            int offs = context.getInvocationOffset();
            String buffer = context.getDocument().get(0, offs);
            //System.out.println("buffer:'"+buffer+"'");
            //System.out.println("offset:"+offs);
            String keyWord = ":";
            String keyWordInfo = "':': noser: autofills the annotation";

            int idx = 0;
            // find the replacement position
            int klen = keyWord.length();
            for (int i = 0; i < klen; i++) {
                String test = keyWord.substring(0, klen - i);
                if (buffer.endsWith(test)) {
                    idx = klen - i;
                    break;
                }
            }
            if (idx != 0) {
                System.out.println("idx:"+idx);
                String replace ="hi there! a longer sample text\nnew line";
                    ICompletionProposal ci = new MyCompletion(buffer, replace,
                            context.getInvocationOffset() - idx, idx, replace
                                    .length(), null, keyWordInfo, null, null);
                    ret.add(ci);
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        return ret;
    }


    public List<ICompletionProposal> computeContextInformation(
            ContentAssistInvocationContext context, IProgressMonitor monitor) {
        return new Vector<ICompletionProposal>();
    }

    public String getErrorMessage() {
        return "Error from SimpleComputer";
    }

    public void sessionEnded() {
        //System.out.println("session ended");
    }

    public void sessionStarted() {
        //System.out.println("session started");
    }

}
