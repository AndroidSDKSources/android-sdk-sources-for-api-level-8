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

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

public class MyCompletion implements ICompletionProposal {

    private String m_displ;
    private String m_replace;
    private int m_offset;
    private int m_replacelen;
    private int m_cursorpos;
    private Image m_img;
    private IContextInformation m_context;
    private String m_addinfo;
    private String fBuffer;

    public MyCompletion(String buffer, String replacementString,
            int replacementOffset, int replacementLength, int cursorPosition,
            Image image, String displayString,
            IContextInformation contextInformation,
            String additionalProposalInfo) {
        m_replace = replacementString;
        m_offset = replacementOffset;
        m_replacelen = replacementLength;
        m_cursorpos = cursorPosition;
        m_img = image;
        fBuffer = buffer;
        m_displ = displayString;
        m_context = contextInformation;
        m_addinfo = additionalProposalInfo;
    }

    public void apply(IDocument document) {
        try {
            MethodSelector ms = new MethodSelector();
            String replace = ms.obtainReplacement(fBuffer);
            if (replace == null) {
                m_cursorpos = 0;
                return;
            }
            m_cursorpos = replace.length();
            document.replace(m_offset, m_replacelen, replace);
        } catch (BadLocationException x) {
            // ignore
        }
    }

    public Point getSelection(IDocument document) {
        return new Point(m_offset + m_cursorpos, 0);
    }

    public IContextInformation getContextInformation() {
        return m_context;
    }

    public Image getImage() {
        return m_img;
    }

    public String getDisplayString() {
        if (m_displ != null) return m_displ;
        return m_replace;
    }

    public String getAdditionalProposalInfo() {
        return m_addinfo;
    }
}
