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

package android.location;

import android.location.Location;
import android.net.NetworkInfo;
import android.os.Bundle;

/**
 * Location Manager's interface for location providers.
 *
 * {@hide}
 */
public interface LocationProviderInterface {
    String getName();
    boolean requiresNetwork();
    boolean requiresSatellite();
    boolean requiresCell();
    boolean hasMonetaryCost();
    boolean supportsAltitude();
    boolean supportsSpeed();
    boolean supportsBearing();
    int getPowerRequirement();
    int getAccuracy();
    boolean isEnabled();
    void enable();
    void disable();
    int getStatus(Bundle extras);
    long getStatusUpdateTime();
    void enableLocationTracking(boolean enable);
    String getInternalState();
    void setMinTime(long minTime);
    void updateNetworkState(int state, NetworkInfo info);
    void updateLocation(Location location);
    boolean sendExtraCommand(String command, Bundle extras);
    void addListener(int uid);
    void removeListener(int uid);
}
