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

package android.view.cts;

import android.graphics.Rect;
import android.test.ActivityInstrumentationTestCase2;
import android.view.FocusFinder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;

@TestTargetClass(FocusFinder.class)
public class FocusFinderTest extends ActivityInstrumentationTestCase2<FocusFinderStubActivity> {

    private FocusFinder mFocusFinder;
    private LinearLayout mLayout;
    private Button mTopWide;
    private Button mMidSkinny1Left;
    private Button mMidSkinny2Right;
    private Button mBottomWide;

    public FocusFinderTest() {
        super("com.android.cts.stub", FocusFinderStubActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mFocusFinder = FocusFinder.getInstance();
        mLayout = getActivity().getLayout();
        mTopWide = getActivity().getTopWide();
        mMidSkinny1Left = getActivity().getMidSkinny1Left();
        mMidSkinny2Right = getActivity().getMidSkinny2Right();
        mBottomWide = getActivity().getBottomWide();
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "test method getInstance",
        method = "getInstance",
        args = {}
    )
    public void testGetInstance() {
        mFocusFinder = null;
        mFocusFinder = FocusFinder.getInstance();
        assertNotNull(mFocusFinder);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "test method findNextFocus",
        method = "findNextFocus",
        args = {ViewGroup.class, View.class, int.class}
    )
    public void testFindNextFocus() {
        Button view = (Button)mFocusFinder.findNextFocus(mLayout, mBottomWide, View.FOCUS_UP);
        assertEquals(mMidSkinny2Right, view);
        view = (Button)mFocusFinder.findNextFocus(mLayout, mMidSkinny2Right, View.FOCUS_LEFT);
        assertEquals(mMidSkinny1Left, view);
        view = (Button)mFocusFinder.findNextFocus(mLayout, mMidSkinny1Left, View.FOCUS_RIGHT);
        assertEquals(mMidSkinny2Right, view);
        view = (Button)mFocusFinder.findNextFocus(mLayout, mTopWide, View.FOCUS_DOWN);
        assertEquals(mMidSkinny1Left, view);
        view = (Button)mFocusFinder.findNextFocus(mLayout, null, View.FOCUS_DOWN);
        assertEquals(mTopWide, view);

        view = (Button)mFocusFinder.findNextFocus(mLayout, null, View.FOCUS_UP);
        assertEquals(mBottomWide, view);

        view = (Button)mFocusFinder.findNextFocus(mLayout, null, View.FOCUS_LEFT);
        assertEquals(mBottomWide, view);

        view = (Button)mFocusFinder.findNextFocus(mLayout, null, View.FOCUS_RIGHT);
        assertEquals(mTopWide, view);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "test method findNextFocusFromRect",
        method = "findNextFocusFromRect",
        args = {ViewGroup.class, Rect.class, int.class}
    )
    public void testFindNextFocusFromRect() {
        Rect mTempRect = new Rect();
        mTempRect.set(0, mTopWide.getTop(), 0, mTopWide.getTop());
        Button view = (Button)mFocusFinder.findNextFocusFromRect(mLayout, mTempRect,
                View.FOCUS_DOWN);
        assertEquals(mTopWide, view);
        mTempRect.set(0, mBottomWide.getTop(), 0, mBottomWide.getTop());
        view = (Button)mFocusFinder.findNextFocusFromRect(mLayout, mTempRect, View.FOCUS_UP);
        assertEquals(mMidSkinny1Left, view);
        mTempRect.set(mMidSkinny1Left.getRight(), 0, mMidSkinny1Left.getRight(), 0);
        view = (Button)mFocusFinder.findNextFocusFromRect(mLayout, mTempRect, View.FOCUS_RIGHT);
        assertEquals(mMidSkinny2Right, view);
        mTempRect.set(mMidSkinny2Right.getLeft(), 0, mMidSkinny2Right.getLeft(), 0);
        view = (Button)mFocusFinder.findNextFocusFromRect(mLayout, mTempRect, View.FOCUS_LEFT);
        assertEquals(mMidSkinny1Left, view);
    }

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "test findNearestTouchable",
        method = "findNearestTouchable",
        args = {ViewGroup.class, int.class, int.class, int.class, int[].class}
    )
    public void testFindNearestTouchable() {
        int[] deltas = new int[2];
        int bound = 3;
        int x = mTopWide.getLeft();
        int y = mTopWide.getTop() - bound;
        Button view = (Button)mFocusFinder.findNearestTouchable(mLayout, x, y, View.FOCUS_DOWN,
                deltas);
        assertEquals(mTopWide, view);
        assertEquals(0, deltas[0]);
        assertEquals(mTopWide.getTop(), deltas[1]);
        deltas = new int[2];
        x = mBottomWide.getLeft();
        y = mBottomWide.getBottom() + bound;
        view = (Button)mFocusFinder.findNearestTouchable(mLayout, x, y, View.FOCUS_UP, deltas);
        assertEquals(mBottomWide, view);
        assertEquals(0, deltas[0]);
        assertEquals(-(y - mBottomWide.getBottom() + 1), deltas[1]);

        deltas = new int[2];
        x = mMidSkinny1Left.getLeft() - bound;
        y = mMidSkinny1Left.getTop();
        view = (Button)mFocusFinder.findNearestTouchable(mLayout, x, y, View.FOCUS_RIGHT, deltas);
        assertEquals(mTopWide, view);
        assertEquals(mMidSkinny1Left.getLeft(), deltas[0]);
        assertEquals(0, deltas[1]);

        deltas = new int[2];
        x = mTopWide.getRight() + bound;
        y = mTopWide.getBottom();
        view = (Button)mFocusFinder.findNearestTouchable(mLayout, x, y, View.FOCUS_LEFT, deltas);
        assertEquals(mTopWide, view);
        assertEquals(-(x - mTopWide.getRight() + 1), deltas[0]);
        assertEquals(0, deltas[1]);
    }

}
