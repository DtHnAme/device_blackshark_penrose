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

package org.lineageos.settings.utils;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.util.Log;

import org.lineageos.settings.R;

import java.util.Random;

public final class SoundUtils {
    private static final String TAG = "SoundUtils";

    private static SoundPool.Builder sBuilder;
    private static SoundPool sSoundPool;
    private static SoundUtils sSoundUtils;

    private SoundUtils() {
        // This class is not supposed to be instantiated
    }

    public static void init(Context context) {
        if (sSoundUtils == null) {
            sSoundUtils = new SoundUtils();
        }
        if (sBuilder == null) {
            sBuilder = new SoundPool.Builder();
        }
        sBuilder.setMaxStreams(AudioAttributes.ALLOW_CAPTURE_BY_SYSTEM);
        AudioAttributes.Builder builder = new AudioAttributes.Builder();
        builder.setLegacyStreamType(AudioAttributes.USAGE_VOICE_COMMUNICATION_SIGNALLING);
        sBuilder.setAudioAttributes(builder.build());
        sSoundPool = sBuilder.build();
        sSoundPool.load(context, R.raw.rise_sound_classic, 1);
        sSoundPool.load(context, R.raw.rise_sound_bullet_loaded, 1);
        sSoundPool.load(context, R.raw.rise_sound_electric, 1);
        sSoundPool.load(context, R.raw.rise_sound_wind_echo, 1);
        sSoundPool.load(context, R.raw.rise_sound_mew, 1);
        sSoundPool.load(context, R.raw.rise_sound_energy_cannon, 1);
        sSoundPool.load(context, R.raw.rise_sound_rifle_gunfire, 1);
        sSoundPool.load(context, R.raw.rise_sound_mecha_transformation, 1);
        sSoundPool.load(context, R.raw.rise_sound_sword_unsheathed, 1);
        sSoundPool.load(context, R.raw.fall_sound_classic, 1);
        sSoundPool.load(context, R.raw.fall_sound_bullet_loaded, 1);
        sSoundPool.load(context, R.raw.fall_sound_electric, 1);
        sSoundPool.load(context, R.raw.fall_sound_wind_echo, 1);
        sSoundPool.load(context, R.raw.fall_sound_mew, 1);
        sSoundPool.load(context, R.raw.fall_sound_energy_cannon, 1);
        sSoundPool.load(context, R.raw.fall_sound_rifle_gunfire, 1);
        sSoundPool.load(context, R.raw.fall_sound_mecha_transformation, 1);
        sSoundPool.load(context, R.raw.fall_sound_sword_unsheathed, 1);
    }

    public static void play(int soundId) {
        if (sSoundPool != null && soundId != 0) {
            sSoundPool.play(soundId == 10 ? randomNum(1, 10) : soundId, 1.0f, 1.0f, 0, 0, 1.0f);
        }
    }

    public static void play(int soundId, boolean isLeft, boolean rise) {
        if (sSoundPool != null && soundId != 0) {
            int newSoundId = !rise ? soundId + 9 : soundId;
            if (isLeft) {
                sSoundPool.play(newSoundId, 1.0f, 0.0f, 0, 0, 1.0f);
            } else {
                sSoundPool.play(newSoundId, 0.0f, 1.0f, 0, 0, 1.0f);
            }
        }
    }

    public static void release() {
        SoundPool soundPool = sSoundPool;
        if (soundPool != null) {
            soundPool.release();
        }
    }
    
    public static int randomNum(int min, int max) {
        if (max > min) {
            return new Random().nextInt(max - min) + min;
        }
        return 0;
    }
}
