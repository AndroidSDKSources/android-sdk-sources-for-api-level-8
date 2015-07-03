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
package android.hardware.cts;

import java.util.List;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.test.AndroidTestCase;
import android.util.Log;

import dalvik.annotation.BrokenTest;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargets;
import dalvik.annotation.ToBeFixed;

@SuppressWarnings("deprecation")
@TestTargetClass(SensorManager.class)
public class SensorManagerTest extends AndroidTestCase {

    private static final String TAG = "SensorManagerTest";
    private static final float[] GRAVITY = { 0, 0, SensorManager.GRAVITY_EARTH };
    private static final double TWO_PI = 2 * Math.PI;
    private static final int MATRIX = 9;
    private static final int VECTOR = 3;
    private static final int MAX_COUNT = 10;
    private static final long TIME_OUT = 10000;
    private static Object mSync;
    private static int mCounter;
    private static boolean mHasNotified;
    private static float[] mR;
    private static float[] mI;
    private static float[] mGeomagnetic;
    private static SensorManager mSensorManager;

    private static int[] mSensorListenerOnChangedSensor;
    private static float[][] mSensorListenerOnChangedValus;
    private static SensorEvent[] mSensorEvents;

    @SuppressWarnings("serial")
    private static class SensorTestTimeOutException extends Exception {
        public SensorTestTimeOutException() {
            super("Time out while waiting for sensor events. "
                    + "Please move the device to generate events.");
        }
    }

    private static void waitForSensorEvent() throws InterruptedException,
            SensorTestTimeOutException {
        mCounter = 0;
        mHasNotified = false;
        synchronized (mSync) {
            if (!mHasNotified) {
                mSync.wait(TIME_OUT);
            }
            if (!mHasNotified) {
                throw new SensorTestTimeOutException();
            }
        }
    }

    private static void addSyncCount() {
        synchronized (mSync) {
            if (++mCounter >= MAX_COUNT) {
                mCounter = MAX_COUNT -1;
                mHasNotified = true;
                mSync.notify();
            }
        }
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mSensorManager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
        mR = new float[MATRIX];
        mI = new float[MATRIX];
        mGeomagnetic = new float[VECTOR];
        mSync = new Object();
        mSensorListenerOnChangedSensor = new int[MAX_COUNT];
        mSensorListenerOnChangedValus = new float[MAX_COUNT][];
        mSensorEvents = new SensorEvent[MAX_COUNT];
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getSensors",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "registerListener",
            args = {SensorListener.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "registerListener",
            args = {SensorListener.class, int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "unregisterListener",
            args = {SensorListener.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "unregisterListener",
            args = {SensorListener.class, int.class}
        )
    })
    @ToBeFixed(bug="1852042", explanation="Error values in onSensorChanged() from system")
    @BrokenTest("Relies on earthquakes or user to generate sensor events")
    public void testSensorManagerOldAPIs() throws InterruptedException, SensorTestTimeOutException {
        assertEquals(SensorManager.SENSOR_ACCELEROMETER
                | SensorManager.SENSOR_MAGNETIC_FIELD
                | SensorManager.SENSOR_ORIENTATION
                | SensorManager.SENSOR_ORIENTATION_RAW,
                mSensorManager.getSensors());

        final SensorListener sensorListener = new SensorListener() {

            public void onAccuracyChanged(int sensor, int accuracy) {
            }

            public void onSensorChanged(int sensor, float[] values) {
                mSensorListenerOnChangedSensor[mCounter] = sensor;
                mSensorListenerOnChangedValus[mCounter] = values;
                if(sensor == SensorManager.SENSOR_ACCELEROMETER) {
                    Log.d(TAG, "Deprecated Accelerometer value X="
                            + values[0] + " Y=" + values[1] + " Z=" + values[2]);
                } else {
                    Log.d(TAG, "Deprecated Orientation value X="
                            + values[0] + " Y=" + values[1] + " Z=" + values[2]);
                }
                addSyncCount();
            }
        };
        mSensorManager.registerListener(sensorListener,
                SensorManager.SENSOR_ACCELEROMETER
                | SensorManager.SENSOR_ORIENTATION
                | SensorManager.SENSOR_ORIENTATION_RAW,
                SensorManager.SENSOR_DELAY_NORMAL);
        waitForSensorEvent();

        mSensorManager.unregisterListener(sensorListener, SensorManager.SENSOR_ACCELEROMETER);
        // Following should unregister sensorListener for all sensor type but it didn't.
        // So we unregisterListener by the other unregister method.
        // mSensorManager.unregisterListener(sensorListener);
        mSensorManager.unregisterListener(sensorListener, SensorManager.SENSOR_ORIENTATION |
                SensorManager.SENSOR_ORIENTATION_RAW);
        try {
            waitForSensorEvent();
            fail("SensorListener have been unregistered, shouldn't get any more sensor events.");
        } catch (SensorTestTimeOutException e) {
            // expected
        }
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getDefaultSensor",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getInclination",
            args = {float[].class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getOrientation",
            args = {float[].class, float[].class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getRotationMatrix",
            args = {float[].class, float[].class, float[].class, float[].class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getSensorList",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "registerListener",
            args = {SensorEventListener.class, Sensor.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "registerListener",
            args = {SensorEventListener.class, Sensor.class, int.class, android.os.Handler.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "remapCoordinateSystem",
            args = {float[].class, int.class, int.class, float[].class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "unregisterListener",
            args = {SensorEventListener.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "unregisterListener",
            args = {SensorEventListener.class, Sensor.class}
        )
    })
    @ToBeFixed(bug="1852042", explanation="Error values in onSensorChanged() from system")
    @BrokenTest("Relies on earthquakes or user to generate sensor events")
    public void testSensorManager() throws InterruptedException, SensorTestTimeOutException {
        final List<Sensor> sensorList = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        assertNotNull(sensorList);
        Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        assertEquals(Sensor.TYPE_ACCELEROMETER, sensor.getType());
        final SensorEventListener sensorAccListener = new SensorEventListener() {
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }

            public void onSensorChanged(SensorEvent event) {
                assertEquals(Sensor.TYPE_ACCELEROMETER, event.sensor.getType());
                Log.d(TAG, "Accelerometer value X="
                        + event.values[0] + " Y=" + event.values[1] + " Z=" + event.values[2]);
                addSyncCount();
            }
        };

        mSensorManager.registerListener(sensorAccListener,
                sensor, SensorManager.SENSOR_DELAY_NORMAL);
        waitForSensorEvent();

        mSensorManager.unregisterListener(sensorAccListener);
        try {
            waitForSensorEvent();
            fail("SensorListener have been unregistered, shouldn't get any more sensor events.");
        } catch (SensorTestTimeOutException e) {
            // expected
        }

        sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        assertEquals(Sensor.TYPE_MAGNETIC_FIELD, sensor.getType());
        final SensorEventListener sensorMagListener = new SensorEventListener() {
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }

            public void onSensorChanged(SensorEvent event) {
                mSensorEvents[mCounter] = event;
                Log.d(TAG, "Magnetic field value X="
                        + event.values[0] + " Y=" + event.values[1] + " Z=" + event.values[2]);
                addSyncCount();
            }
        };

        mSensorManager.registerListener(sensorMagListener, sensor,
                SensorManager.SENSOR_DELAY_FASTEST, null);
        waitForSensorEvent();
        mSensorManager.unregisterListener(sensorMagListener, sensor);

        assertSensorEvents();

        assertTrue(SensorManager.remapCoordinateSystem(mR, SensorManager.AXIS_Y,
                SensorManager.AXIS_MINUS_X, mR));
        final float[] outR = new float[VECTOR];
        assertFalse(SensorManager.remapCoordinateSystem(mR, SensorManager.AXIS_Y,
                SensorManager.AXIS_MINUS_X, outR));
    }

    private static void assertSensorEvents() {
        for (int i = 0; i < MAX_COUNT; i++) {
            assertEquals(Sensor.TYPE_MAGNETIC_FIELD, mSensorEvents[i].sensor.getType());
            mGeomagnetic[0] = mSensorEvents[i].values[0];
            mGeomagnetic[1] = mSensorEvents[i].values[1];
            mGeomagnetic[2] = mSensorEvents[i].values[2];

            assertTrue(SensorManager.getRotationMatrix(mR, mI, GRAVITY, mGeomagnetic));
            float inclination = SensorManager.getInclination(mI);
            assertTrue(Math.abs(inclination) <= TWO_PI);
            float[] values = new float[3];
            float[] orientation = SensorManager.getOrientation(mR, values);
            assertTrue(Math.abs(orientation[0]) <= TWO_PI);
            assertTrue(Math.abs(orientation[1]) <= TWO_PI);
            assertTrue(Math.abs(orientation[2]) <= TWO_PI);
        }
    }
}
