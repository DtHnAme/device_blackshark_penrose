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

import android.content.Context;
import android.view.View;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.PreferenceFragment;
import androidx.preference.ListPreference;
import androidx.preference.SwitchPreference;

import org.lineageos.settings.R;

public class DtsAudioFragment extends PreferenceFragment implements
        OnPreferenceChangeListener {

    public static final String DTS_AUDIO_ENABLE_KEY = "dts_audio_enable";
    public static final String DTS_GEQ_ENABLE_KEY = "dts_geq_enable";
    public static final String DTS_BYPASS_ENABLE_KEY = "dts_bypass_enable";
    public static final String DTS_SOUND_MODE_KEY = "dts_audio_sound_mode";
    public static final String DTS_MUSIC_STYLE_KEY = "dts_music_style";

    private SwitchPreference mDtsAudioEnablePref;
    private SwitchPreference mDtsGeqEnablePref;
    private SwitchPreference mDtsByPassEnablePref;
    private ListPreference mDtsSoundModePref;
    private ListPreference mDtsMusicStylePref;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.dtsaudio_settings);

        mDtsAudioEnablePref = (SwitchPreference) findPreference(DTS_AUDIO_ENABLE_KEY);
        mDtsGeqEnablePref = (SwitchPreference) findPreference(DTS_GEQ_ENABLE_KEY);
        mDtsByPassEnablePref = (SwitchPreference) findPreference(DTS_BYPASS_ENABLE_KEY);
        mDtsSoundModePref = (ListPreference) findPreference(DTS_SOUND_MODE_KEY);
        mDtsMusicStylePref = (ListPreference) findPreference(DTS_MUSIC_STYLE_KEY);

        mDtsAudioEnablePref.setOnPreferenceChangeListener(this);
        mDtsGeqEnablePref.setOnPreferenceChangeListener(this);
        mDtsByPassEnablePref.setOnPreferenceChangeListener(this);
        mDtsSoundModePref.setOnPreferenceChangeListener(this);
        mDtsMusicStylePref.setOnPreferenceChangeListener(this);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        checkPreference();
    }

    public void checkPreference() {
        mDtsAudioEnablePref.setChecked(DtsAudioUtils.getInstance().getDtsEnabled());
        mDtsByPassEnablePref.setChecked(DtsAudioUtils.getInstance().getDtsBypass());
        mDtsGeqEnablePref.setChecked(DtsAudioUtils.getInstance().getGEQEnabled());
        mDtsSoundModePref.setValueIndex(DtsAudioUtils.getInstance().getContentMode() - 3);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        switch (preference.getKey()) {
            case DTS_AUDIO_ENABLE_KEY:
                DtsAudioUtils.getInstance().setDtsEnabled((boolean) newValue);
                break;
            case DTS_GEQ_ENABLE_KEY:
                DtsAudioUtils.getInstance().setGEQEnabled((boolean) newValue);
                break;
            case DTS_BYPASS_ENABLE_KEY:
                DtsAudioUtils.getInstance().setDtsBypass((boolean) newValue);
                break;
            case DTS_MUSIC_STYLE_KEY:
                DtsAudioUtils.getInstance().setMusicStyle(Integer.parseInt(newValue.toString()));
                break;
            case DTS_SOUND_MODE_KEY:
                DtsAudioUtils.getInstance().setContentMode(Integer.parseInt(newValue.toString()));
                break;
            default:
                break;
        }
        //checkPreference();
        return true;
    }

}
