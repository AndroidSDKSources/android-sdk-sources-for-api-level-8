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

package android.os.cts;

import dalvik.annotation.BrokenTest;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;
import android.os.SystemClock;
import android.os.MessageQueue.IdleHandler;
import android.test.AndroidTestCase;

@TestTargetClass(MessageQueue.class)
public class MessageQueueTest extends AndroidTestCase {

    private boolean mResult;
    // Action flag: true means addIdleHanlder, false means removeIdleHanlder
    private boolean mActionFlag;
    private static final long TIMEOUT = 1000;
    private static final long INTERVAL = 50;
    private IdleHandler mIdleHandler = new IdleHandler() {
        public boolean queueIdle() {
            MessageQueueTest.this.mResult = true;
            return true;
        }
    };

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mResult = false;
    }

    /**
     * After calling addIdleHandler (called by MessageQueueTestHelper#doTest), the size of
     * idleHanlder list is not 0 (before calling addIdleHandler, there is no idleHanlder in
     * the test looper we started, that means no idleHanlder with flag mResult), and in doTest,
     * we start a looper, which will queueIdle (Looper.loop()) if idleHanlder list has element,
     * then mResult will be set true. It can make sure addIdleHandler works. If no idleHanlder
     * with flag mResult, mResult will be false.
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "addIdleHandler",
        args = {android.os.MessageQueue.IdleHandler.class}
    )
    @BrokenTest("needs investigation")
    public void testAddIdleHandler() throws RuntimeException, InterruptedException {
        try {
            Looper.myQueue().addIdleHandler(null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        // If mActionFlag is true, doTest will call addIdleHandler
        mActionFlag = true;
        mResult = false;
        MessageQueueTestHelper tester = new MessageQueueTestHelper();
        tester.doTest(TIMEOUT, INTERVAL);

        tester.quit();
        assertTrue(mResult);
    }

    /**
     * In this test method, at the beginning of the LooperThread, we call addIdleHandler then
     * removeIdleHandler, there should be no element in idleHanlder list. So the Looper.loop()
     * will not call queueIdle(), mResult will not be set true.
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "removeIdleHandler",
        args = {android.os.MessageQueue.IdleHandler.class}
    )
    @BrokenTest("needs investigation")
    public void testRemoveIdleHandler() throws RuntimeException, InterruptedException {
        mActionFlag = false;
        mResult = false;
        MessageQueueTestHelper tester = new MessageQueueTestHelper();
        tester.doTest(TIMEOUT, INTERVAL);

        tester.quit();
        assertFalse(mResult);
    }

    /**
     * Use MessageQueue, send message by order
     */
    public void testMessageOrder() throws Exception {

        OrderTestHelper tester = new OrderTestHelper() {
            public void init() {
                super.init();
                long now = SystemClock.uptimeMillis() + 200;
                mLastMessage = 4;
                mCount = 0;

                mHandler.sendMessageAtTime(mHandler.obtainMessage(2), now + 1);
                mHandler.sendMessageAtTime(mHandler.obtainMessage(3), now + 2);
                mHandler.sendMessageAtTime(mHandler.obtainMessage(4), now + 2);
                mHandler.sendMessageAtTime(mHandler.obtainMessage(0), now + 0);
                mHandler.sendMessageAtTime(mHandler.obtainMessage(1), now + 0);
            }

        };
        tester.doTest(1000, 50);
    }

    /**
     * Use MessageQueue, send message at front of queue.
     */
    public void testAtFrontOfQueue() throws Exception {

        OrderTestHelper tester = new OrderTestHelper() {

            public void init() {
                super.init();
                long now = SystemClock.uptimeMillis() + 200;
                mLastMessage = 3;
                mCount = 0;
                mHandler.sendMessageAtTime(mHandler.obtainMessage(3), now);
                mHandler.sendMessageAtFrontOfQueue(mHandler.obtainMessage(2));
                mHandler.sendMessageAtFrontOfQueue(mHandler.obtainMessage(0));
            }

            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == 0) {
                    mHandler.sendMessageAtFrontOfQueue(mHandler.obtainMessage(1));
                }
            }
        };

        tester.doTest(1000, 50);
    }

    /**
     * Helper class used to test addIdleHandler, removeIdleHandler
     */
    private class MessageQueueTestHelper {

        private boolean mDone;
        private Looper mLooper;

        public void doTest(long timeout, long interval) throws InterruptedException {
            (new LooperThread()).start();
            synchronized (this) {
                long now = System.currentTimeMillis();
                long endTime = now + timeout;
                // Wait and frequently check if mDone is set.
                while (!mDone && now < endTime) {
                    wait(interval);
                    now = System.currentTimeMillis();
                }
            }
            mLooper.quit();
            if (!mDone) {
                throw new RuntimeException("test timed out");
            }
        }

        private class LooperThread extends HandlerThread {
            public LooperThread() {
                super("MessengeQueueLooperThread");
            }

            public void onLooperPrepared() {
                mLooper = getLooper();
                if (mActionFlag) {
                    // If mActionFlag is true, just addIdleHandler, and
                    // Looper.loop() will set mResult true.
                    Looper.myQueue().addIdleHandler(mIdleHandler);
                } else {
                    // If mActionFlag is false, addIdleHandler and remove it, then Looper.loop()
                    // will not set mResult true because the idleHandler list is empty.
                    Looper.myQueue().addIdleHandler(mIdleHandler);
                    Looper.myQueue().removeIdleHandler(mIdleHandler);
                }
            }

            @Override
            public void run() {
                super.run();
                synchronized (MessageQueueTestHelper.this) {
                    mDone = true;
                    MessageQueueTestHelper.this.notifyAll();
                }
            }
        }

        public void quit() {
            synchronized (this) {
                mDone = true;
                notifyAll();
            }
        }
    }

    /**
     * Helper class used to test sending message to message queue.
     */
    private class OrderTestHelper {
        Handler mHandler;
        int mLastMessage;
        int mCount;
        private boolean mSuccess;
        private RuntimeException mFailure;
        private boolean mDone;
        private Looper mLooper;

        public void init() {
            mHandler = new Handler() {
                public void handleMessage(Message msg) {
                    OrderTestHelper.this.handleMessage(msg);
                }
            };
        }

        public void handleMessage(Message msg) {
            if (mCount <= mLastMessage) {
                if (msg.what != mCount) {
                    failure(new RuntimeException("Expected message #" + mCount + ", received #"
                            + msg.what));
                } else if (mCount == mLastMessage) {
                    success();
                }

                mCount++;
            } else {
                failure(new RuntimeException("Message received after done, #" + msg.what));
            }
        }

        public void doTest(long timeout, long interval) throws InterruptedException {
            (new LooperThread()).start();

            synchronized (this) {
                long now = System.currentTimeMillis();
                long endTime = now + timeout;
                while (!mDone && now < endTime) {
                    wait(interval);
                    now = System.currentTimeMillis();
                }
            }

            mLooper.quit();

            if (!mDone) {
                throw new RuntimeException("test timed out");
            }
            if (!mSuccess) {
                throw mFailure;
            }
        }

        class LooperThread extends HandlerThread {

            public LooperThread() {
                super("MessengerLooperThread");
            }

            public void onLooperPrepared() {
                init();
                mLooper = getLooper();
            }

            @Override
            public void run() {
                super.run();
                synchronized (OrderTestHelper.this) {
                    mDone = true;
                    if (!mSuccess && mFailure == null) {
                        mFailure = new RuntimeException("no failure exception set");
                    }
                    OrderTestHelper.this.notifyAll();
                }
            }
        }

        public void success() {
            synchronized (this) {
                mSuccess = true;
                quit();
            }
        }

        public void failure(RuntimeException failure) {
            synchronized (this) {
                mSuccess = false;
                mFailure = failure;
                quit();
            }
        }

        private void quit() {
            synchronized (this) {
                mDone = true;
                notifyAll();
            }
        }
    }
}
