/*
 * Copyright (C) 2009 The Android Open Source Project
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

package android.util.cts;

import android.os.Process;
import android.util.EventLog;

import java.io.IOException;
import java.util.ArrayList;

import junit.framework.TestCase;

public class EventLogTest extends TestCase {
    private static final int ANSWER_TAG = 42;
    private static final int PI_TAG = 314;
    private static final int E_TAG = 2718;

    public void testWriteEvent() throws Exception {
        long t0 = getTime();
        EventLog.writeEvent(ANSWER_TAG, 12345);
        EventLog.writeEvent(ANSWER_TAG, 23456L);
        EventLog.writeEvent(ANSWER_TAG, "Test");
        EventLog.writeEvent(ANSWER_TAG, 12345, 23456L, "Test");

        ArrayList<EventLog.Event> events = getEventsSince(t0, new int[] {ANSWER_TAG});
        assertEquals(4, events.size());
        assertEquals(ANSWER_TAG, events.get(0).getTag());
        assertEquals(12345, events.get(0).getData());
        assertEquals(23456L, events.get(1).getData());
        assertEquals("Test", events.get(2).getData());

        Object[] arr = (Object[]) events.get(3).getData();
        assertEquals(3, arr.length);
        assertEquals(12345, arr[0]);
        assertEquals(23456L, arr[1]);
        assertEquals("Test", arr[2]);
    }

    public void testWriteEventWithOversizeValue() throws Exception {
        StringBuilder longString = new StringBuilder();
        for (int i = 0; i < 1000; i++) longString.append("xyzzy");

        Object[] longArray = new Object[1000];
        for (int i = 0; i < 1000; i++) longArray[i] = 12345;

        long t0 = getTime();
        EventLog.writeEvent(ANSWER_TAG, longString.toString());
        EventLog.writeEvent(ANSWER_TAG, "hi", longString.toString());
        EventLog.writeEvent(ANSWER_TAG, 12345, longString.toString());
        EventLog.writeEvent(ANSWER_TAG, 12345L, longString.toString());
        EventLog.writeEvent(ANSWER_TAG, longString.toString(), longString.toString());
        EventLog.writeEvent(ANSWER_TAG, longArray);

        ArrayList<EventLog.Event> events = getEventsSince(t0, new int[] {ANSWER_TAG});
        assertEquals(6, events.size());

        // subtract: log header, type byte, final newline
        final int max = 4096 - 20 - 4 - 1;

        // subtract: string header (type + length)
        String val0 = (String) events.get(0).getData();
        assertEquals(max - 5, val0.length());

        // subtract: array header, "hi" header, "hi", string header
        Object[] arr1 = (Object[]) events.get(1).getData();
        assertEquals(2, arr1.length);
        assertEquals("hi", arr1[0]);
        assertEquals(max - 2 - 5 - 2 - 5, ((String) arr1[1]).length());

        // subtract: array header, int (type + value), string header
        Object[] arr2 = (Object[]) events.get(2).getData();
        assertEquals(2, arr2.length);
        assertEquals(12345, arr2[0]);
        assertEquals(max - 2 - 5 - 5, ((String) arr2[1]).length());

        // subtract: array header, long, string header
        Object[] arr3 = (Object[]) events.get(3).getData();
        assertEquals(2, arr3.length);
        assertEquals(12345L, arr3[0]);
        assertEquals(max - 2 - 9 - 5, ((String) arr3[1]).length());

        // subtract: array header, string header (second string is dropped entirely)
        Object[] arr4 = (Object[]) events.get(4).getData();
        assertEquals(1, arr4.length);
        assertEquals(max - 2 - 5, ((String) arr4[0]).length());

        Object[] arr5 = (Object[]) events.get(5).getData();
        assertEquals(255, arr5.length);
        assertEquals(12345, arr5[0]);
        assertEquals(12345, arr5[arr5.length - 1]);
    }

    public void testWriteNullEvent() throws Exception {
        long t0 = getTime();
        EventLog.writeEvent(ANSWER_TAG, (String) null);
        EventLog.writeEvent(ANSWER_TAG, 12345, (String) null);

        ArrayList<EventLog.Event> events = getEventsSince(t0, new int[] {ANSWER_TAG});
        assertEquals(2, events.size());
        assertEquals("NULL", events.get(0).getData());

        Object[] arr = (Object[]) events.get(1).getData();
        assertEquals(2, arr.length);
        assertEquals(12345, arr[0]);
        assertEquals("NULL", arr[1]);
    }

    public void testReadEvents() throws Exception {
        long t0 = getTime();
        EventLog.writeEvent(ANSWER_TAG, 0);
        long t1 = getTime();
        EventLog.writeEvent(PI_TAG, "1");
        long t2 = getTime();
        EventLog.writeEvent(E_TAG, 2);
        long t3 = getTime();

        // Exclude E_TAG
        ArrayList<EventLog.Event> events = getEventsSince(t0, new int[] {ANSWER_TAG, PI_TAG});
        assertEquals(2, events.size());

        assertEquals(Process.myPid(), events.get(0).getProcessId());
        assertEquals(Process.myTid(), events.get(0).getThreadId());
        assertTrue(events.get(0).getTimeNanos() >= t0 * 1000000L);
        assertTrue(events.get(0).getTimeNanos() <= t1 * 1000000L);
        assertEquals(ANSWER_TAG, events.get(0).getTag());
        assertEquals(0, events.get(0).getData());

        assertEquals(Process.myPid(), events.get(1).getProcessId());
        assertEquals(Process.myTid(), events.get(1).getThreadId());
        assertTrue(events.get(1).getTimeNanos() >= t1 * 1000000L);
        assertTrue(events.get(1).getTimeNanos() <= t2 * 1000000L);
        assertEquals(PI_TAG, events.get(1).getTag());
        assertEquals("1", events.get(1).getData());
    }

    public void testGetTagName() throws Exception {
        assertEquals("answer", EventLog.getTagName(ANSWER_TAG));
        assertEquals("pi", EventLog.getTagName(PI_TAG));
        assertEquals("e", EventLog.getTagName(E_TAG));
        assertEquals(null, EventLog.getTagName(999999999));
    }

    public void testGetTagCode() throws Exception {
        assertEquals(ANSWER_TAG, EventLog.getTagCode("answer"));
        assertEquals(PI_TAG, EventLog.getTagCode("pi"));
        assertEquals(E_TAG, EventLog.getTagCode("e"));
        assertEquals(-1, EventLog.getTagCode("does_not_exist"));
    }

    private long getTime() throws InterruptedException {
        // The precision of currentTimeMillis is poor compared to event timestamps
        Thread.sleep(20);
        return System.currentTimeMillis() - 10;
    }

    private ArrayList<EventLog.Event> getEventsSince(long since, int[] tags) throws IOException {
        ArrayList<EventLog.Event> tmp = new ArrayList<EventLog.Event>();
        EventLog.readEvents(tags, tmp);

        ArrayList<EventLog.Event> out = new ArrayList<EventLog.Event>();
        for (EventLog.Event event : tmp) {
            if (event.getTimeNanos() / 1000000 >= since) {
                out.add(event);
            }
        }
        return out;
    }
}
