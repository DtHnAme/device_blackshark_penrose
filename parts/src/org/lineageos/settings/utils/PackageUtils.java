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

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.IBinder;
import android.os.UserHandle;
import android.util.Log;

public final class PackageUtils {
    private static final String TAG = "PackageUtils";

    private PackageManager mPackageManager;
    private ActivityManager mActivityManager;

    public PackageUtils(Context context) {
        mPackageManager = context.getPackageManager();
        mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    }

    public String getTopPackageName() {
        ComponentName name = mActivityManager.getRunningTasks(1).get(0).topActivity;
        if (name == null)
            return "";
        return name.getPackageName();
    }

    public ApplicationInfo getApplicationInfo(String packageName) {
        ApplicationInfo applicationInfo = null;
         try {
            applicationInfo = mPackageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
        } catch (Exception ignored) {
        }
        return applicationInfo;
    }
    
    public String getTopAppName() {
        String appName = "";
        try {
            appName = getApplicationInfo(getTopPackageName()).loadLabel(mPackageManager).toString();
        } catch (Exception ignored) {
        }
        return appName;
    }
    
    public Drawable getTopAppIcon() {
        Drawable appIcon = new ColorDrawable(Color.TRANSPARENT);
        try {
            appIcon = getApplicationInfo(getTopPackageName()).loadIcon(mPackageManager);
        } catch (Exception ignored) {
        }
        return appIcon;
    }
    
    public String getAppName(String packageName) {
        String appName = "";
        try {
            appName = getApplicationInfo(packageName).loadLabel(mPackageManager).toString();
        } catch (Exception ignored) {
        }
        return appName;
    }
    
    public Drawable getAppIcon(String packageName) {
        Drawable appIcon = new ColorDrawable(Color.TRANSPARENT);
        try {
            appIcon = getApplicationInfo(packageName).loadIcon(mPackageManager);
        } catch (Exception ignored) {
        }
        return appIcon;
    }

}
