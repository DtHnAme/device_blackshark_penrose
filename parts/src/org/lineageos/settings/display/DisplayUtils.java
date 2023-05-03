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

package org.lineageos.settings.display;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import org.lineageos.settings.utils.FileUtils;

public final class DisplayUtils {
    public static final String DC_DIMMING_ENABLE_KEY = "dc_dimming_enable";
    public static final String HBM_ENABLE_KEY = "hbm_enable";
    public static final String DISPLAY_PARAM_NODE = "/sys/class/drm/card0-DSI-1/disp_param";

    private static DisplayUtils mDisplayUtils;
    private static SharedPreferences mSharedPreferences;

    private DisplayUtils() {
        // This class is not supposed to be instantiated
    }

    public static DisplayUtils getInstance() {
        if (mDisplayUtils == null)
            mDisplayUtils = new DisplayUtils();
        return mDisplayUtils;
    }

    public void initialize(Context context) {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        setHBMEnabled(getHBMEnabled());
        setDcDimmingEnabled(getDcDimmingEnabled());
    }

    public boolean getHBMEnabled() {
        return mSharedPreferences.getBoolean(HBM_ENABLE_KEY, false);
    }

    public void setHBMEnabled(boolean enabled) {
        FileUtils.writeLine(DISPLAY_PARAM_NODE, enabled ? "0x10000":"0xF0000");
        mSharedPreferences.edit().putBoolean(HBM_ENABLE_KEY, enabled).commit();
    }

    public boolean getDcDimmingEnabled() {
        return mSharedPreferences.getBoolean(DC_DIMMING_ENABLE_KEY, false);
    }

    public void setDcDimmingEnabled(boolean enabled) {
        FileUtils.writeLine(DISPLAY_PARAM_NODE, enabled ? "0x40000":"0x50000");
        mSharedPreferences.edit().putBoolean(DC_DIMMING_ENABLE_KEY, enabled).commit();
    }

}
