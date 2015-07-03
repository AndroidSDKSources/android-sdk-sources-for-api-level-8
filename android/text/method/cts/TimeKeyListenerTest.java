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

import android.app.Activity;
import android.app.Instrumentation;
import android.test.ActivityInstrumentationTestCase2;
import android.text.InputType;
import android.text.method.TimeKeyListener;
import android.view.KeyEvent;
import android.widget.TextView;

@TestTargetClass(TimeKeyListener.class)
public class TimeKeyListenerTest extends
        ActivityInstrumentationTestCase2<KeyListenerStubActivity> {
    private Activity mActivity;
    private Instrumentation mInstrumentation;
    private TextView mTextView;

    public TimeKeyListenerTest(){
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
        method = "TimeKeyListener",
        args = {}
    )
    public void testConstructor() {
        new TimeKeyListener();
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getInstance",
        args = {}
    )
    public void testGetInstance() {
        TimeKeyListener listener1 = TimeKeyListener.getInstance();
        TimeKeyListener listener2 = TimeKeyListener.getInstance();

        assertNotNull(listener1);
        assertNotNull(listener2);
        assertSame(listener1, listener2);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getAcceptedChars",
        args = {}
    )
    public void testGetAcceptedChars() {
        MyTimeKeyListener timeKeyListener = new MyTimeKeyListener();
        TextMethodUtils.assertEquals(TimeKeyListener.CHARACTERS,
                timeKeyListener.getAcceptedChars());
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getInputType",
        args = {}
    )
    public void testGetInputType() {
        TimeKeyListener listener = TimeKeyListener.getInstance();
        int expected = InputType.TYPE_CLASS_DATETIME
                | InputType.TYPE_DATETIME_VARIATION_TIME;
        assertEquals(expected, listener.getInputType());
    }

    /**
     * Scenario description:
     * 1. Press '1' key and check if the content of TextView becomes "1"
     * 2. Press '2' key and check if the content of TextView becomes "12"
     * 3. Press 'a' key and check if the content of TextView becomes "12a"
     * 4. Press an unaccepted key if it exists and this key could not be entered.
     * 5. Press 'm' key and check if the content of TextView becomes "12am"
     * 6. remove TimeKeyListener, '1' key will not be accepted.
     */
    public void testTimeKeyListener() {
        final TimeKeyListener timeKeyListener = TimeKeyListener.getInstance();

        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                mTextView.setKeyListener(timeKeyListener);
                mTextView.requestFocus();
            }
        });
        mInstrumentation.waitForIdleSync();
        assertEquals("", mTextView.getText().toString());

        // press '1' key.
        sendKeys(KeyEvent.KEYCODE_1);
        assertEquals("1", mTextView.getText().toString());

        // press '2' key.
        sendKeys(KeyEvent.KEYCODE_2);
        assertEquals("12", mTextView.getText().toString());

        // press 'a' key.
        sendKeys(KeyEvent.KEYCODE_A);
        assertEquals("12a", mTextView.getText().toString());

        // press an unaccepted key if it exists.
        int keyCode = TextMethodUtils.getUnacceptedKeyCode(TimeKeyListener.CHARACTERS);
        if (-1 != keyCode) {
            sendKeys(keyCode);
            assertEquals("12a", mTextView.getText().toString());
        }

        // press 'm' key.
        sendKeys(KeyEvent.KEYCODE_M);
        assertEquals("12am", mTextView.getText().toString());

        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                mTextView.setKeyListener(null);
                mTextView.requestFocus();
            }
        });
        mInstrumentation.waitForIdleSync();

        // press '1' key.
        sendKeys(KeyEvent.KEYCODE_1);
        assertEquals("12am", mTextView.getText().toString());
    }

    private class MyTimeKeyListener extends TimeKeyListener {
        @Override
        protected char[] getAcceptedChars() {
            return super.getAcceptedChars();
        }
    }
}
