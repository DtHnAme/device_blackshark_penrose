/*
 * Copyright (C) 2016 The CyanogenMod Project
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

package org.lineageos.settings.shoulderkey;

import static org.lineageos.settings.shoulderkey.ShoulderKeyFragment.SOUND_EFFECT_KEY;

import android.hardware.input.InputManager;
import android.content.Context;
import android.util.Log;

import androidx.preference.PreferenceManager;

import org.lineageos.settings.R;
import org.lineageos.settings.utils.SoundUtils;

public final class ShoulderKeyManager implements InputManager.OnSmartSwitchChangedListener {
    private static final int LEFT_KEY_CODE = 131;
    private static final int RIGHT_KEY_CODE = 132;

    private static final String TAG = "ShoulderKeyManager";

    private static ShoulderKeyManager mShoulderKeyManager;

    private InputManager mInputManager;
    private Context mContext;

    private int mSoundId = 0;
    private int mRandomSoundId = 0;
    private boolean mKeyLeft;

    private ShoulderKeyManager() {
        // This class is not supposed to be instantiated
    }

    public static ShoulderKeyManager getInstance() {
        if (mShoulderKeyManager == null)
            mShoulderKeyManager = new ShoulderKeyManager();
        return mShoulderKeyManager;
    }

    public void register(Context context) {
        if (mInputManager == null) {
            mInputManager = (InputManager) context.getSystemService(InputManager.class);
            Log.i(TAG, "register InputManager");
        }
        mContext = context;
        mInputManager.registerOnSmartSwitchChangedListener(this, null);
        //mInputManager.registerOnKeyEventListener(this, null);
    }

    public void unregister() {
        if (mInputManager != null) {
            mInputManager.unregisterOnSmartSwitchChangedListener(this);
            //mInputManager.unregisterOnKeyEventListener(this);
        }
    }

    @Override
    public void onSmartSwitchChanged(long whenNanos, boolean smartSwitchState) {
        boolean isLeft = whenNanos == 1;
        Log.i(TAG, "onSmartSwitchChanged: " + whenNanos + " " + smartSwitchState);
        mSoundId = getSoundId();
        if (mSoundId == 10) {
            if (mKeyLeft != isLeft && smartSwitchState) {
                mRandomSoundId = SoundUtils.randomNum(1, 10);
            }
            mSoundId = mRandomSoundId;
            mKeyLeft = isLeft;
        }
        SoundUtils.play(mSoundId, isLeft, smartSwitchState);
    }

    private int getSoundId() {
        return Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(mContext)
                .getString(SOUND_EFFECT_KEY, ""));        
    }

}
