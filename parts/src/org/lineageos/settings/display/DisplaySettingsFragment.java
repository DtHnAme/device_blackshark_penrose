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

package org.lineageos.settings.display;

import android.content.Context;
import android.os.Bundle;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.PreferenceFragment;
import androidx.preference.SwitchPreference;

import org.lineageos.settings.R;
import org.lineageos.settings.utils.FileUtils;

public class DisplaySettingsFragment extends PreferenceFragment implements
        OnPreferenceChangeListener {

    private SwitchPreference mDcDimmingPreference;
    private SwitchPreference mHBMPreference;
    private static final String DC_DIMMING_ENABLE_KEY = "dc_dimming_enable";
    private static final String HBM_ENABLE_KEY = "hbm_enable";
    private static final String DISPLAY_PARAM_NODE = "/sys/class/drm/card0-DSI-1/disp_param";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.display_settings);
        mDcDimmingPreference = (SwitchPreference) findPreference(DC_DIMMING_ENABLE_KEY);
        mHBMPreference = (SwitchPreference) findPreference(HBM_ENABLE_KEY);
        if (FileUtils.fileExists(DISPLAY_PARAM_NODE)) {
            mDcDimmingPreference.setEnabled(true);
            mHBMPreference.setEnabled(true);
            mDcDimmingPreference.setOnPreferenceChangeListener(this);
            mHBMPreference.setOnPreferenceChangeListener(this);
        } else {
            mDcDimmingPreference.setSummary(R.string.kernel_not_supported);
            mHBMPreference.setSummary(R.string.kernel_not_supported);
            mDcDimmingPreference.setEnabled(false);
            mHBMPreference.setEnabled(false);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (DC_DIMMING_ENABLE_KEY.equals(preference.getKey())) {
            DisplayUtils.getInstance().setDcDimmingEnabled((Boolean) newValue);
        }
        if (HBM_ENABLE_KEY.equals(preference.getKey())) {
            DisplayUtils.getInstance().setHBMEnabled((Boolean) newValue);
        }
        return true;
    }
}
