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

package android.text.method.cts;

import com.android.cts.stub.R;

import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.ToBeFixed;

import android.app.Activity;
import android.app.Instrumentation;
import android.test.ActivityInstrumentationTestCase2;
import android.text.Editable;
import android.text.InputType;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.MultiTapKeyListener;
import android.text.method.TextKeyListener.Capitalize;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.TextView.BufferType;

@TestTargetClass(MultiTapKeyListener.class)
public class MultiTapKeyListenerTest extends
        ActivityInstrumentationTestCase2<KeyListenerStubActivity> {
    /**
     * time out of MultiTapKeyListener. longer than 2000ms in case the system is sluggish.
     */
    private static final long TIME_OUT = 3000;
    private Activity mActivity;
    private Instrumentation mInstrumentation;
    private TextView mTextView;

    public MultiTapKeyListenerTest() {
        super("com.android.cts.stub", KeyListenerStubActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mActivity = getActivity();
        mInstrumentation = getInstrumentation();
        mTextView = (TextView) mActivity.findViewById(R.id.keylistener_textview);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "MultiTapKeyListener",
        args = {android.text.method.TextKeyListener.Capitalize.class, boolean.class}
    )
    public void testConstructor() {
        new MultiTapKeyListener(Capitalize.NONE, true);

        new MultiTapKeyListener(Capitalize.WORDS, false);

        new MultiTapKeyListener(null, false);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "it is a non-operation method",
        method = "onSpanAdded",
        args = {Spannable.class, Object.class, int.class, int.class}
    )
    public void testOnSpanAdded() {
        final MockMultiTapKeyListener multiTapKeyListener
                = new MockMultiTapKeyListener(Capitalize.CHARACTERS, true);
        final Spannable text = new SpannableStringBuilder("123456");

        assertFalse(multiTapKeyListener.hadAddedSpan());
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                mTextView.setKeyListener(multiTapKeyListener);
                mTextView.setText(text, BufferType.EDITABLE);
            }
        });
        mInstrumentation.waitForIdleSync();

        assertTrue(multiTapKeyListener.hadAddedSpan());
    }

    @TestTargetNew(
        level = TestLevel.PARTIAL,
        method = "onSpanChanged",
        args = {Spannable.class, Object.class, int.class, int.class, int.class, int.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete, " +
            "should add @throws clause into javadoc")
    public void testOnSpanChanged() {
        final MultiTapKeyListener multiTapKeyListener
                = MultiTapKeyListener.getInstance(true, Capitalize.CHARACTERS);
        final Spannable text = new SpannableStringBuilder("123456");
        multiTapKeyListener.onSpanChanged(text, Selection.SELECTION_END, 0, 0, 0, 0);

        try {
            multiTapKeyListener.onSpanChanged(null, Selection.SELECTION_END, 0, 0, 0, 0);
            fail("should throw NullPointerException.");
        } catch (NullPointerException e) {
            // expected.
        }
    }

    @Override
    public void sendRepeatedKeys(int... keys) {
        super.sendRepeatedKeys(keys);
        waitForListenerTimeout();
    }

    /**
     * Wait for TIME_OUT, or listener will accept key event as multi tap rather than a new key.
     */
    private void waitForListenerTimeout() {
        try {
            Thread.sleep(TIME_OUT);
        } catch (InterruptedException e) {
            fail("thrown unexpected InterruptedException when sleep.");
        }
    }

    /**
     * Check point when Capitalize.NONE and autotext is false:
     * 1. press KEYCODE_4 twice, text is "h".
     * 2. press KEYCODE_3 twice, text is "he".
     * 3. press KEYCODE_5 three times, text is "hel".
     * 4. press KEYCODE_5 three times, text is "hell".
     * 5. press KEYCODE_8 twice, text is "hellu".
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "onKeyDown",
        args = {View.class, Editable.class, int.class, KeyEvent.class}
    )
    public void testPressKey1() {
        final MultiTapKeyListener multiTapKeyListener
                = MultiTapKeyListener.getInstance(false, Capitalize.NONE);

        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                mTextView.setText("", BufferType.EDITABLE);
                mTextView.requestFocus();
                Selection.setSelection((Editable) mTextView.getText(), 0, 0);
                mTextView.setKeyListener(multiTapKeyListener);
            }
        });
        mInstrumentation.waitForIdleSync();
        assertEquals("", mTextView.getText().toString());

        sendRepeatedKeys(2, KeyEvent.KEYCODE_4);
        assertEquals("h", mTextView.getText().toString());

        sendRepeatedKeys(2, KeyEvent.KEYCODE_3);
        assertEquals("he", mTextView.getText().toString());

        sendRepeatedKeys(3, KeyEvent.KEYCODE_5);
        assertEquals("hel", mTextView.getText().toString());

        sendRepeatedKeys(3, KeyEvent.KEYCODE_5);
        assertEquals("hell", mTextView.getText().toString());

        sendRepeatedKeys(2, KeyEvent.KEYCODE_8);
        assertEquals("hellu", mTextView.getText().toString());
    }

    /**
     * Check point when Capitalize.NONE and autotext is true:
     * 1. press KEYCODE_4 twice, text is "h".
     * 2. press KEYCODE_3 twice, text is "he".
     * 3. press KEYCODE_5 three times, text is "hel".
     * 4. press KEYCODE_5 three times, text is "hell".
     * 5. press KEYCODE_8 twice, text should not be "hellu".
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "onKeyDown",
        args = {View.class, Editable.class, int.class, KeyEvent.class}
    )
    @ToBeFixed(bug = "1738241", explanation = "can not correct spelling automatically")
    public void testPressKey2() {
        final MultiTapKeyListener multiTapKeyListener
                = MultiTapKeyListener.getInstance(true, Capitalize.NONE);

        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                mTextView.setText("", BufferType.EDITABLE);
                mTextView.requestFocus();
                Selection.setSelection((Editable) mTextView.getText(), 0, 0);
                mTextView.setKeyListener(multiTapKeyListener);
            }
        });
        mInstrumentation.waitForIdleSync();
        assertEquals("", mTextView.getText().toString());

        sendRepeatedKeys(2, KeyEvent.KEYCODE_4);
        assertEquals("h", mTextView.getText().toString());

        sendRepeatedKeys(2, KeyEvent.KEYCODE_3);
        assertEquals("he", mTextView.getText().toString());

        sendRepeatedKeys(3, KeyEvent.KEYCODE_5);
        assertEquals("hel", mTextView.getText().toString());

        sendRepeatedKeys(3, KeyEvent.KEYCODE_5);
        assertEquals("hell", mTextView.getText().toString());

        sendRepeatedKeys(2, KeyEvent.KEYCODE_8);
//        assertFalse("hellu".equals(mEditText.getText().toString())); issue 1738241
    }

    /**
     * Check point when Capitalize.SENTENCES and autotext is false:
     * 1. press KEYCODE_4 twice, text is "H".
     * 2. press KEYCODE_3 twice, text is "He".
     * 3. press KEYCODE_5 three times, text is "Hel".
     * 4. press KEYCODE_5 three times, text is "Hell".
     * 5. press KEYCODE_8 twice, text is "Hellu".
     * 6. press KEYCODE_1 once, text is "Hellu.".
     * 7. press KEYCODE_POUND once, text is "Hellu. ".
     * 8. press KEYCODE_6 once, text is "Hellu. M".
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "onKeyDown",
        args = {View.class, Editable.class, int.class, KeyEvent.class}
    )
    public void testPressKey3() {
        final MultiTapKeyListener multiTapKeyListener
                = MultiTapKeyListener.getInstance(false, Capitalize.SENTENCES);

        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                mTextView.setText("", BufferType.EDITABLE);
                mTextView.requestFocus();
                Selection.setSelection((Editable) mTextView.getText(), 0, 0);
                mTextView.setKeyListener(multiTapKeyListener);
            }
        });
        mInstrumentation.waitForIdleSync();
        assertEquals("", mTextView.getText().toString());

        sendRepeatedKeys(2, KeyEvent.KEYCODE_4);
        assertEquals("H", mTextView.getText().toString());

        sendRepeatedKeys(2, KeyEvent.KEYCODE_3);
        assertEquals("He", mTextView.getText().toString());

        sendRepeatedKeys(3, KeyEvent.KEYCODE_5);
        assertEquals("Hel", mTextView.getText().toString());

        sendRepeatedKeys(3, KeyEvent.KEYCODE_5);
        assertEquals("Hell", mTextView.getText().toString());

        sendRepeatedKeys(2, KeyEvent.KEYCODE_8);
        assertEquals("Hellu", mTextView.getText().toString());

        sendRepeatedKeys(1, KeyEvent.KEYCODE_1);
        assertEquals("Hellu.", mTextView.getText().toString());

        sendRepeatedKeys(1, KeyEvent.KEYCODE_POUND);
        assertEquals("Hellu. ", mTextView.getText().toString());

        sendRepeatedKeys(1, KeyEvent.KEYCODE_6);
        assertEquals("Hellu. M", mTextView.getText().toString());
    }

    /**
     * Check point when Capitalize.SENTENCES and autotext is true:
     * 1. press KEYCODE_4 twice, text is "H".
     * 2. press KEYCODE_3 twice, text is "He".
     * 3. press KEYCODE_5 three times, text is "Hel".
     * 4. press KEYCODE_5 three times, text is "Hell".
     * 5. press KEYCODE_8 twice, text is "Hellu".
     * 6. press KEYCODE_1 once, text is "Hellu.".
     * 7. press KEYCODE_POUND once, text is "Hellu. ".
     * 8. press KEYCODE_6 once, text should not be "Hellu. M".
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "onKeyDown",
        args = {View.class, Editable.class, int.class, KeyEvent.class}
    )
    @ToBeFixed(bug = "1738241", explanation = "can not correct spelling automatically")
    public void testPressKey4() {
        final MultiTapKeyListener multiTapKeyListener
                = MultiTapKeyListener.getInstance(true, Capitalize.SENTENCES);

        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                mTextView.setText("", BufferType.EDITABLE);
                mTextView.requestFocus();
                Selection.setSelection((Editable) mTextView.getText(), 0, 0);
                mTextView.setKeyListener(multiTapKeyListener);
            }
        });
        mInstrumentation.waitForIdleSync();
        assertEquals("", mTextView.getText().toString());

        sendRepeatedKeys(2, KeyEvent.KEYCODE_4);
        assertEquals("H", mTextView.getText().toString());

        sendRepeatedKeys(2, KeyEvent.KEYCODE_3);
        assertEquals("He", mTextView.getText().toString());

        sendRepeatedKeys(3, KeyEvent.KEYCODE_5);
        assertEquals("Hel", mTextView.getText().toString());

        sendRepeatedKeys(3, KeyEvent.KEYCODE_5);
        assertEquals("Hell", mTextView.getText().toString());

        sendRepeatedKeys(2, KeyEvent.KEYCODE_8);
        assertEquals("Hellu", mTextView.getText().toString());

        sendRepeatedKeys(1, KeyEvent.KEYCODE_1);
//        assertFalse("Hellu.".equals(mEditText.getText().toString())); issue 1738241
//
//        sendRepeatedKeys(1, KeyEvent.KEYCODE_POUND);
//        assertEquals("Hellu. ", mEditText.getText().toString());
//
//        sendRepeatedKeys(1, KeyEvent.KEYCODE_6);
//        assertEquals("Hellu. M", mEditText.getText().toString());
    }

    /**
     * Check point when Capitalize.WORDS and autotext is false:
     * 1. press KEYCODE_4 twice, text is "H".
     * 2. press KEYCODE_3 twice, text is "He".
     * 3. press KEYCODE_5 three times, text is "Hel".
     * 4. press KEYCODE_5 three times, text is "Hell".
     * 5. press KEYCODE_8 twice, text is "Hellu".
     * 6. press KEYCODE_POUND once, text is "Hellu ".
     * 7. press KEYCODE_6 once, text is "Hellu M".
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "onKeyDown",
        args = {View.class, Editable.class, int.class, KeyEvent.class}
    )
    public void testPressKey5() {
        final MultiTapKeyListener multiTapKeyListener
                = MultiTapKeyListener.getInstance(false, Capitalize.WORDS);

        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                mTextView.setText("", BufferType.EDITABLE);
                mTextView.requestFocus();
                Selection.setSelection((Editable) mTextView.getText(), 0, 0);
                mTextView.setKeyListener(multiTapKeyListener);
            }
        });
        mInstrumentation.waitForIdleSync();
        assertEquals("", mTextView.getText().toString());

        sendRepeatedKeys(2, KeyEvent.KEYCODE_4);
        assertEquals("H", mTextView.getText().toString());

        sendRepeatedKeys(2, KeyEvent.KEYCODE_3);
        assertEquals("He", mTextView.getText().toString());

        sendRepeatedKeys(3, KeyEvent.KEYCODE_5);
        assertEquals("Hel", mTextView.getText().toString());

        sendRepeatedKeys(3, KeyEvent.KEYCODE_5);
        assertEquals("Hell", mTextView.getText().toString());

        sendRepeatedKeys(2, KeyEvent.KEYCODE_8);
        assertEquals("Hellu", mTextView.getText().toString());

        sendRepeatedKeys(1, KeyEvent.KEYCODE_POUND);
        assertEquals("Hellu ", mTextView.getText().toString());

        sendRepeatedKeys(1, KeyEvent.KEYCODE_6);
        assertEquals("Hellu M", mTextView.getText().toString());
    }

    /**
     * Check point when Capitalize.CHARACTERS and autotext is false:
     * 1. press KEYCODE_4 twice, text is "H".
     * 2. press KEYCODE_3 twice, text is "HE".
     * 3. press KEYCODE_5 three times, text is "HEL".
     * 4. press KEYCODE_5 three times, text is "HELL".
     * 5. press KEYCODE_8 twice, text is "HELLU".
     * 6. press KEYCODE_POUND once, text is "HELLU ".
     * 7. press KEYCODE_6 once, text is "HELLU M".
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "onKeyDown",
        args = {View.class, Editable.class, int.class, KeyEvent.class}
    )
    public void testPressKey6() {
        final MultiTapKeyListener multiTapKeyListener
                = MultiTapKeyListener.getInstance(false, Capitalize.CHARACTERS);

        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                mTextView.setText("", BufferType.EDITABLE);
                mTextView.requestFocus();
                Selection.setSelection((Editable) mTextView.getText(), 0, 0);
                mTextView.setKeyListener(multiTapKeyListener);
            }
        });
        mInstrumentation.waitForIdleSync();
        assertEquals("", mTextView.getText().toString());

        sendRepeatedKeys(2, KeyEvent.KEYCODE_4);
        assertEquals("H", mTextView.getText().toString());

        sendRepeatedKeys(2, KeyEvent.KEYCODE_3);
        assertEquals("HE", mTextView.getText().toString());

        sendRepeatedKeys(3, KeyEvent.KEYCODE_5);
        assertEquals("HEL", mTextView.getText().toString());

        sendRepeatedKeys(3, KeyEvent.KEYCODE_5);
        assertEquals("HELL", mTextView.getText().toString());

        sendRepeatedKeys(2, KeyEvent.KEYCODE_8);
        assertEquals("HELLU", mTextView.getText().toString());

        sendRepeatedKeys(1, KeyEvent.KEYCODE_POUND);
        assertEquals("HELLU ", mTextView.getText().toString());

        sendRepeatedKeys(1, KeyEvent.KEYCODE_6);
        assertEquals("HELLU M", mTextView.getText().toString());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getInstance",
        args = {boolean.class, android.text.method.TextKeyListener.Capitalize.class}
    )
    public void testGetInstance() {
        MultiTapKeyListener listener1 = MultiTapKeyListener.getInstance(false, Capitalize.NONE);
        MultiTapKeyListener listener2 = MultiTapKeyListener.getInstance(false, Capitalize.NONE);
        MultiTapKeyListener listener3 = MultiTapKeyListener.getInstance(false, Capitalize.WORDS);
        MultiTapKeyListener listener4 = MultiTapKeyListener.getInstance(true, Capitalize.NONE);

        assertNotNull(listener1);
        assertNotNull(listener2);
        assertSame(listener1, listener2);

        assertNotSame(listener1, listener3);
        assertNotSame(listener4, listener3);
        assertNotSame(listener4, listener1);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "it is a non-operation method.",
        method = "onSpanRemoved",
        args = {android.text.Spannable.class, java.lang.Object.class, int.class, int.class}
    )
    public void testOnSpanRemoved() {
        MultiTapKeyListener multiTapKeyListener =
                new MultiTapKeyListener(Capitalize.CHARACTERS, true);
        final Spannable text = new SpannableStringBuilder("123456");
        multiTapKeyListener.onSpanRemoved(text, new Object(), 0, 0);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getInputType",
        args = {}
    )
    public void testGetInputType() {
        MultiTapKeyListener listener = MultiTapKeyListener.getInstance(false, Capitalize.NONE);
        int expected = InputType.TYPE_CLASS_TEXT;
        assertEquals(expected, listener.getInputType());

        listener = MultiTapKeyListener.getInstance(true, Capitalize.CHARACTERS);
        expected = InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
                | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT;
        assertEquals(expected, listener.getInputType());
    }

    private class MockMultiTapKeyListener extends MultiTapKeyListener {
        private boolean mHadAddedSpan;

        public MockMultiTapKeyListener(Capitalize cap, boolean autotext) {
            super(cap, autotext);
        }

        @Override
        public void onSpanAdded(Spannable s, Object what, int start, int end) {
            mHadAddedSpan = true;
            super.onSpanAdded(s, what, start, end);
        }

        public boolean hadAddedSpan() {
            return mHadAddedSpan;
        }
    }
}
