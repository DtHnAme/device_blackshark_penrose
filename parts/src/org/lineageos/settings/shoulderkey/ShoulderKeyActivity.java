/*
 * Copyright (C) 2015-2016 The CyanogenMod Project
 *               2017 The LineageOS Project
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

import android.app.Fragment;
import android.content.ComponentName;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.content.Intent;

import androidx.fragment.app.FragmentActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragment;

import com.android.settingslib.collapsingtoolbar.CollapsingToolbarBaseActivity;
import com.android.settingslib.widget.R;

public class ShoulderKeyActivity extends CollapsingToolbarBaseActivity  
    implements PreferenceFragment.OnPreferenceStartFragmentCallback {

    private static final String TAG_SHOULDER_KEY = "shoulder_key";
    public static final String PACKAGE_NAME = "org.lineageos.settings";
    public static final String EXTRA_SHOW_FRAGMENT = ":settings:show_fragment";
    public static final String EXTRA_SHOW_FRAGMENT_TITLE = ":settings:show_fragment_title";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Fragment fragment = null;
        String fragmentClass = getIntent().getStringExtra(EXTRA_SHOW_FRAGMENT);
        String titleText = getIntent().getStringExtra(EXTRA_SHOW_FRAGMENT_TITLE);
        
        if (fragmentClass != null) {
            fragment = Fragment.instantiate(this, fragmentClass);
        }

        if (titleText != null) {
            setTitle(titleText);
        }
        
        if (fragment == null) {
            fragment = new ShoulderKeyFragment(); 
        }

        getFragmentManager().beginTransaction().replace(R.id.content_frame,
                fragment, TAG_SHOULDER_KEY).commit();
    }
    
    @Override
    public boolean onPreferenceStartFragment(PreferenceFragment caller, Preference pref) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(PACKAGE_NAME, PACKAGE_NAME + ".shoulderkey.ShoulderKeyActivity"));
        intent.putExtra(EXTRA_SHOW_FRAGMENT, pref.getFragment());
        intent.putExtra(EXTRA_SHOW_FRAGMENT_TITLE, pref.getTitle());

        startActivity(intent);
        return true;
    }

}
