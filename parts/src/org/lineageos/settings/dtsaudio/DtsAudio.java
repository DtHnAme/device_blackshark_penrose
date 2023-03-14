/*
 * Copyright (C) 2018 The LineageOS Project
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

package org.lineageos.settings.dtsaudio;

import android.media.audiofx.AudioEffect;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

public class DtsAudio extends AudioEffect {
    private static final String TAG = "DtsAudio";

    private static UUID EFFECT_TYPE_DTS_AUDIO = UUID.fromString("1d4033c0-8557-11df-9f2d-0002a5d5c51b");
    private static UUID EFFECT_UUID_IMPLEMENTATION_DTS_AUDIO = UUID.fromString("146edfc0-7ed2-11e4-80eb-0002a5d5c51b");

    private static final int DTS_GET_ENABLED = 0;
    private static final int DTS_SET_ENABLED = 1;
    private static final int DTS_SET_GEQ_GAIN = 3;
    private static final int DTS_SET_GEQ_ENABLED = 5;
    private static final int DTS_GET_GEQ_ENABLED = 6;
    private static final int DTS_GET_DTS_LICENSE_IS_VALID = 12;
    private static final int DTS_SET_CONTENT_MODE = 221;
    private static final int DTS_GET_CONTENT_MODE = 222;

    public DtsAudio(int priority, int audioSession) {
        super(EFFECT_TYPE_DTS_AUDIO, EFFECT_UUID_IMPLEMENTATION_DTS_AUDIO, priority, audioSession);
    }

    public int getDtsEnabled() throws IllegalStateException,
            IllegalArgumentException, UnsupportedOperationException {
        int[] value = new int[1];
        checkStatus(getParameter(DTS_GET_ENABLED, value));
        return value[0];
    }

    public void setDtsEnabled(int enable) throws IllegalStateException,
            IllegalArgumentException, UnsupportedOperationException {
        checkStatus(setParameter(DTS_SET_ENABLED, enable));
    }

    public int getGEQEnabled() throws IllegalStateException,
            IllegalArgumentException, UnsupportedOperationException {
        int[] value = new int[1];
        checkStatus(getParameter(DTS_GET_GEQ_ENABLED, value));
        return value[0];
    }

    public void setGEQEnabled(int enable) throws IllegalStateException,
            IllegalArgumentException, UnsupportedOperationException {
        checkStatus(setParameter(DTS_SET_GEQ_ENABLED, enable));
    }

    public int getContentMode() throws IllegalStateException,
            IllegalArgumentException, UnsupportedOperationException {
        int[] value = new int[1];
        checkStatus(getParameter(DTS_GET_CONTENT_MODE, value));
        return value[0];
    }

    public void setContentMode(int mode) throws IllegalStateException,
            IllegalArgumentException, UnsupportedOperationException {
        checkStatus(setParameter(DTS_SET_CONTENT_MODE, mode));
    }

    public void setGEQGain(int i, int i2) throws IllegalStateException,
            IllegalArgumentException, UnsupportedOperationException {
        int[] mode = new int[]{DTS_SET_GEQ_GAIN};
        int[] value = new int[]{i, i2};
        checkStatus(setParameter(mode, value));
    }

    public int getDtsLicenseExists() throws IllegalStateException,
            IllegalArgumentException, UnsupportedOperationException {
        int[] value = new int[1];
        checkStatus(getParameter(DTS_GET_DTS_LICENSE_IS_VALID, value));
        return value[0];
    }

}
