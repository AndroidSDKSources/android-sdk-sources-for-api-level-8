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

package android.app.cts;

import android.app.DatePickerDialog;
import android.app.Instrumentation;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.test.ActivityInstrumentationTestCase2;
import android.text.TextUtils.TruncateAt;
import android.view.KeyEvent;
import android.widget.DatePicker;
import android.widget.TextView;
import dalvik.annotation.BrokenTest;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargets;

@TestTargetClass(DatePickerDialog.class)
public class DatePickerDialogTest extends ActivityInstrumentationTestCase2<DialogStubActivity> {

    private Instrumentation mInstrumentation;
    private DialogStubActivity mActivity;
    private int mOrientation;

    public DatePickerDialogTest() {
        super("com.android.cts.stub", DialogStubActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mInstrumentation = getInstrumentation();
        mActivity = getActivity();
        mOrientation = mActivity.getRequestedOrientation();
    }

    @Override
    protected void tearDown() throws Exception {
        if (mActivity != null) {
            mActivity.setRequestedOrientation(mOrientation);
            mActivity.finish();
        }
        super.tearDown();
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "DatePickerDialog",
            args = {Context.class, int.class, OnDateSetListener.class, int.class, int.class,
                    int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onSaveInstanceState",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onClick",
            args = {DialogInterface.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onDateChanged",
            args = {DatePicker.class, int.class, int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onRestoreInstanceState",
            args = {android.os.Bundle.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "show",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "updateDate",
            args = {int.class, int.class, int.class}
        )
    })
    @BrokenTest("assume layout of DatePickerDialog")
    public void testDatePickerDialogWithTheme() throws Exception {
        doTestDatePickerDialog(DialogStubActivity.TEST_DATEPICKERDIALOG_WITH_THEME);
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "DatePickerDialog",
            args = {Context.class, OnDateSetListener.class, int.class, int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onSaveInstanceState",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onClick",
            args = {DialogInterface.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onDateChanged",
            args = {DatePicker.class, int.class, int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onRestoreInstanceState",
            args = {android.os.Bundle.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "show",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "updateDate",
            args = {int.class, int.class, int.class}
        )
    })
    @BrokenTest("assume layout of DatePickerDialog")
    public void testDatePickerDialog() throws Exception {
        doTestDatePickerDialog(DialogStubActivity.TEST_DATEPICKERDIALOG);
    }

    private void doTestDatePickerDialog(int index) throws Exception {
        popDialog(index);
        final DatePickerDialog datePickerDialog = (DatePickerDialog) mActivity.getDialog();
        assertTrue(datePickerDialog.isShowing());
        final TextView title = (TextView) datePickerDialog.findViewById(
                com.android.internal.R.id.alertTitle);
        assertEquals(TruncateAt.END, title.getEllipsize());

        // move the focus to the 'set' button
        mInstrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN);
        // move the focus up to the '-' button under the month
        mInstrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_UP);
        // decrement the month (moves focus to date field)
        mInstrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_ENTER);
        // move focus down to '-' button under the month
        mInstrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN);
        // move focus down to 'set' button
        mInstrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN);
        // click the 'set' button to accept changes
        mInstrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_ENTER);

        mInstrumentation.waitForIdleSync();
        assertTrue(mActivity.onClickCalled);
        assertEquals(mActivity.updatedYear, mActivity.INITIAL_YEAR);
        assertEquals(mActivity.updatedMonth + 1, mActivity.INITIAL_MONTH);
        assertEquals(mActivity.updatedDay, mActivity.INITIAL_DAY_OF_MONTH);
        assertTrue(DialogStubActivity.onDateChangedCalled);

        mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        mInstrumentation.waitForIdleSync();
        mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mInstrumentation.waitForIdleSync();
        assertTrue(mActivity.onSaveInstanceStateCalled);
        assertTrue(DialogStubActivity.onRestoreInstanceStateCalled);
    }

    private void popDialog(int index) {
        assertTrue(index > 0);

        while (index != 0) {
            sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
            index--;
        }

        sendKeys(KeyEvent.KEYCODE_DPAD_CENTER);
    }

}
