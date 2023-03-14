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

package org.lineageos.settings.shoulderkey;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.PreferenceFragment;
import androidx.preference.SwitchPreference;

import org.lineageos.settings.R;
import org.lineageos.settings.utils.SoundUtils;
import org.lineageos.settings.utils.NotificationUtils;

public class ShoulderKeyFragment extends PreferenceFragment implements
        OnPreferenceChangeListener {

    public static final String SOUND_EFFECT_KEY = "shoulder_key_sound_effect";
    public static final String PREVENT_ACCIDENTAL_KEY = "prevent_accidental_touch";
    public static final String KEY_MAPPING_GLOBAL_MODE_KEY = "key_mapping_global_mode";
    public static final String APP_SETTINGS_KEY = "key_mapping_app_settings";

    private Preference mSoundEffectPref;
    private Preference mPreventAccidentalPref;
    private Preference mAppSettingsPref;

    private SwitchPreference mKeyMappingGlobalModePref;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.shoulder_key_settings);
        getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);

        mSoundEffectPref = findPreference(SOUND_EFFECT_KEY);
        mPreventAccidentalPref = findPreference(PREVENT_ACCIDENTAL_KEY);
        mKeyMappingGlobalModePref = findPreference(KEY_MAPPING_GLOBAL_MODE_KEY);
        mAppSettingsPref = findPreference(APP_SETTINGS_KEY);

        mSoundEffectPref.setOnPreferenceChangeListener(this);
        mPreventAccidentalPref.setOnPreferenceChangeListener(this);
        mKeyMappingGlobalModePref.setOnPreferenceChangeListener(this);
        mKeyMappingGlobalModePref.setChecked(ShoulderKeyService.getInstance().getKeyMappingGlobalMode());
        mAppSettingsPref.setEnabled(!mKeyMappingGlobalModePref.isChecked());
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Log.i(ShoulderKeyFragment.class.getSimpleName(), "onPreferenceChange: " + preference.getKey());
        switch (preference.getKey()) {
            case SOUND_EFFECT_KEY:
                SoundUtils.play(Integer.parseInt(newValue.toString()));
                break;
            case PREVENT_ACCIDENTAL_KEY:
                if (!(boolean) newValue)
                    NotificationUtils.getInstance().removeNotification();
                break;
            case KEY_MAPPING_GLOBAL_MODE_KEY:
                ShoulderKeyService.getInstance().setKeyMappingGlobalMode((boolean) newValue);
                mAppSettingsPref.setEnabled(!(boolean) newValue);
                break;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getActivity().onBackPressed();
            return true;
        }
        return false;
    }

}
