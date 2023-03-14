/*
 * Copyright (C) 2018,2020 The LineageOS Project
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

import android.content.Context;
import android.media.AudioManager;
import android.util.Log;

import org.lineageos.settings.R;

public final class DtsAudioUtils {
    protected static DtsAudio mDtsAudio;
    private static AudioManager mAudioManager;
    private static DtsAudioUtils mDtsAudioUtils;
    private static Context mContext;

    private static final String TAG = "DtsAudioUtils";
    private static int[] mDTSStyles = new int[]{
        R.array.dtsNoStyle,
        R.array.dtsRockStyle,
        R.array.dtsJazzStyle,
        R.array.dtsFashionStyle,
        R.array.dtsClassicalStyle,
        R.array.dtsBruceStyle,
        R.array.dtsElectronicStyle,
        R.array.dtsCountryStyle,
        R.array.dtsDanceStyle,
    };

    private DtsAudioUtils() {
        // This class is not supposed to be instantiated
    }

    public static DtsAudioUtils getInstance() {
        if (mDtsAudio == null)
            mDtsAudio = new DtsAudio(0, 0);
        if (mDtsAudioUtils == null)
            mDtsAudioUtils = new DtsAudioUtils();
        return mDtsAudioUtils;
    }

    public void initialize(Context context) {
        mContext = context;
        if (mAudioManager == null)
            mAudioManager = mContext.getSystemService(AudioManager.class);
    }

    protected boolean getDtsBypass() {
        try {
            String parameters = mAudioManager.getParameters("DTS_BYPASS");
            Log.d(TAG, "getParameter result :" + parameters + " and DTS bypass mode status " + !parameters.contains("off"));
            return !parameters.contains("off");
        } catch (Exception ignored) {
            return false;
        }
    }

    protected void setDtsBypass(boolean enable) {
        setParamByAudioManager("DTS_ENABLE_STATE", !enable);
        setParamByAudioManager("DTS_BYPASS", enable);
        setDtsEnabled(!enable);
    }

    protected void setParamByAudioManager(String param, boolean enable) {
        Log.i(TAG, "setParamByAudioManager: " + param + "=" + (enable ? "on" : "off"));
        mAudioManager.setParameters(param + "=" + (enable ? "on" : "off"));
    }

    protected void setDtsEnabled(boolean enable) {
        mDtsAudio.setDtsEnabled(enable ? 1 : 0);
        setParamByAudioManager("DTS_ENABLE_STATE", enable);
    }

    protected boolean getDtsEnabled() {
        return mDtsAudio.getDtsEnabled() == 1;
    }

    protected void setGEQEnabled(boolean enable) {
        mDtsAudio.setGEQEnabled(enable ? 1 : 0);
    }

    protected boolean getGEQEnabled() {
        return mDtsAudio.getGEQEnabled() == 1;
    }

    protected int getContentMode() {
        return mDtsAudio.getContentMode();
    }

    protected void setContentMode(int mode) {
        Log.i(TAG, "setContentMode:" + mode);
        mAudioManager.setParameters("dtsContentMode=" + String.valueOf(mode));
        mDtsAudio.setContentMode(mode);
    }

    protected void setGEQGain(int i, int i2) {
        Log.i(TAG, "setGEQGain: " + i + " " + i2);
        mDtsAudio.setGEQGain(i, i2);
    }

    protected void setMusicStyle(int style) {
        Log.i(TAG, "setMusicStyle:" + style);
        setGEQEnabled(true);
        for (int i = 0; i <= 9; i++) {
            setGEQGain(i, mContext.getResources().getIntArray(mDTSStyles[style])[i]);
        }
    }

}
