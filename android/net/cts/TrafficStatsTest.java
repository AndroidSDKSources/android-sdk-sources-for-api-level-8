/*
 * Copyright (C) 2010 The Android Open Source Project
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

package android.net.cts;

import android.os.Process;
import android.net.TrafficStats;
import android.test.AndroidTestCase;

import dalvik.annotation.TestTargets;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargetClass;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;

@TestTargetClass(TrafficStats.class)
public class TrafficStatsTest extends AndroidTestCase {
    @TestTargets({
        @TestTargetNew(level = TestLevel.SUFFICIENT, method = "getMobileTxPackets"),
        @TestTargetNew(level = TestLevel.SUFFICIENT, method = "getMobileRxPackets"),
        @TestTargetNew(level = TestLevel.SUFFICIENT, method = "getMobileTxBytes"),
        @TestTargetNew(level = TestLevel.SUFFICIENT, method = "getMobileRxBytes")
    })
    public void testGetMobileStats() {
        // We can't assume a mobile network is even present in this test, so
        // we simply assert that a valid value is returned.

        assertTrue(TrafficStats.getMobileTxPackets() == TrafficStats.UNSUPPORTED ||
                   TrafficStats.getMobileTxPackets() >= 0);
        assertTrue(TrafficStats.getMobileRxPackets() == TrafficStats.UNSUPPORTED ||
                   TrafficStats.getMobileRxPackets() >= 0);
        assertTrue(TrafficStats.getMobileTxBytes() == TrafficStats.UNSUPPORTED ||
                   TrafficStats.getMobileTxBytes() >= 0);
        assertTrue(TrafficStats.getMobileRxBytes() == TrafficStats.UNSUPPORTED ||
                   TrafficStats.getMobileRxBytes() >= 0);
    }

    @TestTargets({
        @TestTargetNew(level = TestLevel.PARTIAL_COMPLETE, method = "getTotalTxPackets"),
        @TestTargetNew(level = TestLevel.PARTIAL_COMPLETE, method = "getTotalRxPackets"),
        @TestTargetNew(level = TestLevel.PARTIAL_COMPLETE, method = "getTotalTxBytes"),
        @TestTargetNew(level = TestLevel.PARTIAL_COMPLETE, method = "getTotalRxBytes"),
        @TestTargetNew(level = TestLevel.PARTIAL_COMPLETE, method = "getUidTxBytes"),
        @TestTargetNew(level = TestLevel.PARTIAL_COMPLETE, method = "getUidRxBytes")
    })
    public void testTrafficStatsWithHostLookup() {
        long txPacketsBefore = TrafficStats.getTotalTxPackets();
        long rxPacketsBefore = TrafficStats.getTotalRxPackets();
        long txBytesBefore = TrafficStats.getTotalTxBytes();
        long rxBytesBefore = TrafficStats.getTotalRxBytes();
        long uidTxBytesBefore = TrafficStats.getUidTxBytes(Process.myUid());
        long uidRxBytesBefore = TrafficStats.getUidRxBytes(Process.myUid());

        // Look up random hostnames in a wildcard domain owned by Google.
        // This will require a DNS request, which should generate traffic.

        int found = 0;
        Random r = new Random();
        for (int i = 0; i < 10; i++) {
            try {
                String host = "test" + r.nextInt(100000) + ".clients.google.com";
                InetAddress[] addr = InetAddress.getAllByName(host);
                if (addr.length > 0) found++;
            } catch (UnknownHostException e) {
                // Ignore -- our purpose is not to test network connectivity,
                // and we'd rather have false positives than a flaky test.
            }
        }

        long txPacketsAfter = TrafficStats.getTotalTxPackets();
        long rxPacketsAfter = TrafficStats.getTotalRxPackets();
        long txBytesAfter = TrafficStats.getTotalTxBytes();
        long rxBytesAfter = TrafficStats.getTotalRxBytes();
        long uidTxBytesAfter = TrafficStats.getUidTxBytes(Process.myUid());
        long uidRxBytesAfter = TrafficStats.getUidRxBytes(Process.myUid());

        // Make some conservative assertions about the data used:
        // each successful resolution should exchange at least one packet,
        // and at least 20 bytes in each direction.

        assertTrue("txp: " + txPacketsBefore + " [" + found + "] " + txPacketsAfter,
                   txPacketsAfter >= txPacketsBefore + found);
        assertTrue("rxp: " + rxPacketsBefore + " [" + found + "] " + rxPacketsAfter,
                   rxPacketsAfter >= rxPacketsBefore + found);
        assertTrue("txb: " + txBytesBefore + " [" + found + "] " + txBytesAfter,
                   txBytesAfter >= txBytesBefore + found * 20);
        assertTrue("rxb: " + rxBytesBefore + " [" + found + "] " + rxBytesAfter,
                   rxBytesAfter >= rxBytesBefore + found * 20);
        assertTrue("uidtxb: " + uidTxBytesBefore + " [" + found + "] " + uidTxBytesAfter,
                   uidTxBytesAfter >= uidTxBytesBefore + found * 20);
        assertTrue("uidrxb: " + uidRxBytesBefore + " [" + found + "] " + uidRxBytesAfter,
                   uidRxBytesAfter >= uidRxBytesBefore + found * 20);
    }

    @TestTargets({
        @TestTargetNew(level = TestLevel.PARTIAL_COMPLETE, method = "getMobileTxPackets"),
        @TestTargetNew(level = TestLevel.PARTIAL_COMPLETE, method = "getMobileRxPackets"),
        @TestTargetNew(level = TestLevel.PARTIAL_COMPLETE, method = "getMobileTxBytes"),
        @TestTargetNew(level = TestLevel.PARTIAL_COMPLETE, method = "getMobileRxBytes"),
        @TestTargetNew(level = TestLevel.PARTIAL_COMPLETE, method = "getTotalTxPackets"),
        @TestTargetNew(level = TestLevel.PARTIAL_COMPLETE, method = "getTotalRxPackets"),
        @TestTargetNew(level = TestLevel.PARTIAL_COMPLETE, method = "getTotalTxBytes"),
        @TestTargetNew(level = TestLevel.PARTIAL_COMPLETE, method = "getTotalRxBytes"),
        @TestTargetNew(level = TestLevel.PARTIAL_COMPLETE, method = "getUidTxBytes"),
        @TestTargetNew(level = TestLevel.PARTIAL_COMPLETE, method = "getUidRxBytes")
    })
    public void testTrafficStatsForLocalhost() throws IOException {
        long mobileTxPacketsBefore = TrafficStats.getTotalTxPackets();
        long mobileRxPacketsBefore = TrafficStats.getTotalRxPackets();
        long mobileTxBytesBefore = TrafficStats.getTotalTxBytes();
        long mobileRxBytesBefore = TrafficStats.getTotalRxBytes();
        long totalTxPacketsBefore = TrafficStats.getTotalTxPackets();
        long totalRxPacketsBefore = TrafficStats.getTotalRxPackets();
        long totalTxBytesBefore = TrafficStats.getTotalTxBytes();
        long totalRxBytesBefore = TrafficStats.getTotalRxBytes();
        long uidTxBytesBefore = TrafficStats.getUidTxBytes(Process.myUid());
        long uidRxBytesBefore = TrafficStats.getUidRxBytes(Process.myUid());

        // Transfer 1MB of data across an explicitly localhost socket.

        final ServerSocket server = new ServerSocket(0);
        new Thread("TrafficStatsTest.testTrafficStatsForLocalhost") {
            @Override
            public void run() {
                try {
                    Socket socket = new Socket("localhost", server.getLocalPort());
                    OutputStream out = socket.getOutputStream();
                    byte[] buf = new byte[1024];
                    for (int i = 0; i < 1024; i++) out.write(buf);
                    out.close();
                    socket.close();
                } catch (IOException e) {
                }
            }
        }.start();

        try {
            Socket socket = server.accept();
            InputStream in = socket.getInputStream();
            byte[] buf = new byte[1024];
            int read = 0;
            while (read < 1048576) {
                int n = in.read(buf);
                assertTrue("Unexpected EOF", n > 0);
                read += n;
            }
        } finally {
            server.close();
        }

        long mobileTxPacketsAfter = TrafficStats.getTotalTxPackets();
        long mobileRxPacketsAfter = TrafficStats.getTotalRxPackets();
        long mobileTxBytesAfter = TrafficStats.getTotalTxBytes();
        long mobileRxBytesAfter = TrafficStats.getTotalRxBytes();
        long totalTxPacketsAfter = TrafficStats.getTotalTxPackets();
        long totalRxPacketsAfter = TrafficStats.getTotalRxPackets();
        long totalTxBytesAfter = TrafficStats.getTotalTxBytes();
        long totalRxBytesAfter = TrafficStats.getTotalRxBytes();
        long uidTxBytesAfter = TrafficStats.getUidTxBytes(Process.myUid());
        long uidRxBytesAfter = TrafficStats.getUidRxBytes(Process.myUid());

        // Localhost traffic should *not* count against mobile or total stats.
        // There might be some other traffic, but nowhere near 1MB.

        assertTrue("mtxp: " + mobileTxPacketsBefore + " -> " + mobileTxPacketsAfter,
               mobileTxPacketsAfter >= mobileTxPacketsBefore &&
               mobileTxPacketsAfter <= mobileTxPacketsBefore + 500);
        assertTrue("mrxp: " + mobileRxPacketsBefore + " -> " + mobileRxPacketsAfter,
               mobileRxPacketsAfter >= mobileRxPacketsBefore &&
               mobileRxPacketsAfter <= mobileRxPacketsBefore + 500);
        assertTrue("mtxb: " + mobileTxBytesBefore + " -> " + mobileTxBytesAfter,
               mobileTxBytesAfter >= mobileTxBytesBefore &&
               mobileTxBytesAfter <= mobileTxBytesBefore + 200000);
        assertTrue("mrxb: " + mobileRxBytesBefore + " -> " + mobileRxBytesAfter,
               mobileRxBytesAfter >= mobileRxBytesBefore &&
               mobileRxBytesAfter <= mobileRxBytesBefore + 200000);

        assertTrue("ttxp: " + totalTxPacketsBefore + " -> " + totalTxPacketsAfter,
               totalTxPacketsAfter >= totalTxPacketsBefore &&
               totalTxPacketsAfter <= totalTxPacketsBefore + 500);
        assertTrue("trxp: " + totalRxPacketsBefore + " -> " + totalRxPacketsAfter,
               totalRxPacketsAfter >= totalRxPacketsBefore &&
               totalRxPacketsAfter <= totalRxPacketsBefore + 500);
        assertTrue("ttxb: " + totalTxBytesBefore + " -> " + totalTxBytesAfter,
               totalTxBytesAfter >= totalTxBytesBefore &&
               totalTxBytesAfter <= totalTxBytesBefore + 200000);
        assertTrue("trxb: " + totalRxBytesBefore + " -> " + totalRxBytesAfter,
               totalRxBytesAfter >= totalRxBytesBefore &&
               totalRxBytesAfter <= totalRxBytesBefore + 200000);

        // Localhost traffic *does* count against per-UID stats.
        assertTrue("uidtxb: " + uidTxBytesBefore + " -> " + uidTxBytesAfter,
               uidTxBytesAfter >= uidTxBytesBefore + 1048576);
        assertTrue("uidrxb: " + uidRxBytesBefore + " -> " + uidRxBytesAfter,
               uidRxBytesAfter >= uidRxBytesBefore + 1048576);
    }
}
