/*
 * Copyright (C) 2008 Google Inc.
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

package android.tests.getinfo;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

public class DeviceInfoInstrument extends Instrumentation {
    private static final String PHONE_NUMBER = "phoneNumber";
    public static final String LOCALES = "locales";
    private static final String IMSI = "imsi";
    private static final String IMEI = "imei";
    private static final String NETWORK = "network";
    public static final String KEYPAD = "keypad";
    public static final String NAVIGATION = "navigation";
    public static final String TOUCH_SCREEN = "touch_screen";
    private static final String SCREEN_Y_DENSITY = "screen_Y_density";
    private static final String SCREEN_X_DENSITY = "screen_X_density";
    private static final String SCREEN_DENSITY = "screen_density";
    private static final String SCREEN_HEIGHT = "screen_height";
    private static final String SCREEN_WIDTH = "screen_width";
    private static final String VERSION_SDK = "version_sdk";
    private static final String VERSION_RELEASE = "version_release";
    private static final String VERSION_INCREMENTAL = "version_incremental";
    private static final String BUILD_ABI = "build_abi";
    private static final String BUILD_ABI2 = "build_abi2";
    private static final String BUILD_FINGERPRINT = "build_fingerprint";
    private static final String BUILD_TAGS = "build_tags";
    private static final String BUILD_TYPE = "build_type";
    private static final String BUILD_MODEL = "build_model";
    private static final String BUILD_BRAND = "build_brand";
    private static final String BUILD_BOARD = "build_board";
    private static final String BUILD_DEVICE = "build_device";
    private static final String PRODUCT_NAME = "product_name";
    private static final String BUILD_ID = "build_id";
    private static Bundle mResults = new Bundle();

    public DeviceInfoInstrument() {
        super();
    }

    @Override
    public void onCreate(Bundle arguments) {
        start();
    }

    @Override
    public void onStart() {

        addResult(BUILD_ID, Build.ID);
        addResult(PRODUCT_NAME, Build.PRODUCT);
        addResult(BUILD_DEVICE, Build.DEVICE);
        addResult(BUILD_BOARD, Build.BOARD);
        addResult(BUILD_BRAND, Build.BRAND);
        addResult(BUILD_MODEL, Build.MODEL);
        addResult(BUILD_TYPE, Build.TYPE);
        addResult(BUILD_TAGS, Build.TAGS);
        addResult(BUILD_FINGERPRINT, Build.FINGERPRINT);
        addResult(BUILD_ABI, Build.CPU_ABI);
        addResult(BUILD_ABI2, Build.CPU_ABI2);

        addResult(VERSION_INCREMENTAL, Build.VERSION.INCREMENTAL);
        addResult(VERSION_RELEASE, Build.VERSION.RELEASE);
        addResult(VERSION_SDK, Build.VERSION.SDK);

        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) getContext().getSystemService(
                Context.WINDOW_SERVICE);
        Display d = wm.getDefaultDisplay();
        d.getMetrics(metrics);
        addResult(SCREEN_WIDTH, metrics.widthPixels);
        addResult(SCREEN_HEIGHT, metrics.heightPixels);
        addResult(SCREEN_DENSITY, metrics.density);
        addResult(SCREEN_X_DENSITY, metrics.xdpi);
        addResult(SCREEN_Y_DENSITY, metrics.ydpi);

        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClass(this.getContext(), DeviceInfoActivity.class);

        DeviceInfoActivity activity = (DeviceInfoActivity) startActivitySync(intent);
        waitForIdleSync();
        activity.waitForAcitityToFinish();

        TelephonyManager tm = (TelephonyManager) getContext().getSystemService(
                Context.TELEPHONY_SERVICE);
        // network
        String network = tm.getNetworkOperatorName();
        addResult(NETWORK, network);

        // imei
        String imei = tm.getDeviceId();
        addResult(IMEI, imei);

        // imsi
        String imsi = tm.getSubscriberId();
        addResult(IMSI, imsi);

        // phone number
        String phoneNumber = tm.getLine1Number();
        addResult(PHONE_NUMBER, phoneNumber);

        finish(Activity.RESULT_OK, mResults);
    }

    /**
     * Add string result.
     *
     * @param key the string of the key name.
     * @param value string value.
     */
    static void addResult(final String key, final String value){
        mResults.putString(key, value);
    }

    /**
     * Add integer result.
     *
     * @param key the string of the key name.
     * @param value integer value.
     */
    private void addResult(final String key, final int value){
        mResults.putInt(key, value);
    }

    /**
     * Add float result.
     *
     * @param key the string of the key name.
     * @param value float value.
     */
    private void addResult(final String key, final float value){
        mResults.putFloat(key, value);
    }
}
