/*
 * Copyright (C) 2023 The LineageOS Project
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.settings.shoulderkey;

import static org.lineageos.settings.shoulderkey.ShoulderKeyFragment.KEY_MAPPING_GLOBAL_MODE_KEY;

import android.app.ActivityManager;
import android.app.ActivityTaskManager;
import android.app.TaskStackListener;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Rect;
import android.util.Log;
import android.view.Display;
import android.view.IRotationWatcher;
import android.view.Surface;
import android.view.WindowManager;
import android.view.WindowManagerGlobal;

import androidx.preference.PreferenceManager;

import org.lineageos.settings.utils.PackageUtils;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.io.File;

public final class ShoulderKeyUtils {
    private static final String TAG = "ShoulderKeyUtils";
    private static final String GLOBAL_MODE_PACKAGE = "global.keymapping.package";
    public static final boolean DEBUG = false;

    private static Context mContext;

    private ShoulderKeyDBHelper mDB;
    private PackageUtils mPackageUtils;
    private WindowManager mWindowManager;

    private List<ShoulderKeyInfo> mInfoList = new ArrayList<>();

    private CoordinateChanageListener mListener;

    private int[] mCenter;
    private int mRotation = 0;
    private String mPackageName = "";

    private final TaskStackListener mTaskListener = new TaskStackListener() {
        @Override
        public void onTaskStackChanged() {
            try {
                final ActivityTaskManager.RootTaskInfo focusedStack =
                        ActivityTaskManager.getService().getFocusedRootTaskInfo();
                if (focusedStack != null && focusedStack.topActivity != null) {
                    ComponentName taskComponentName = focusedStack.topActivity;
                    String packageName = taskComponentName.getPackageName();
                    if (mPackageName != packageName) {
                        mPackageName = packageName;
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "cannot updatePackageName");
            }
        }
    };

    private final IRotationWatcher mWatcher = new IRotationWatcher.Stub() {
        @Override
        public void onRotationChanged(int rotation) {
            switch (rotation) {
                case Surface.ROTATION_0:
                case Surface.ROTATION_180:
                    rotation = 0;
                    break;
                case Surface.ROTATION_90:
                case Surface.ROTATION_270:
                    rotation = 1;
                    break;
            }
            if (mRotation != rotation) {
                mRotation = rotation;
            }
        }
    };

    public interface CoordinateChanageListener {
        void onCoordinateChanged();
    }

    public void setCoordinateChanageListener(CoordinateChanageListener listener) {
        mListener = listener;
    }

    public ShoulderKeyUtils(ShoulderKeyDBHelper db, Context context) {
        mDB = db;
        mContext = context;
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mPackageUtils = new PackageUtils(context);
        
        Rect rect = mWindowManager.getCurrentWindowMetrics().getBounds();
        mCenter = new int[]{rect.centerX(), rect.centerY()};
        mInfoList = mDB.getAllAppsKeyInfo();
    }

    public ShoulderKeyUtils(ShoulderKeyDBHelper db, Context context, boolean enableTaskListener) {
        this(db, context);
        if (enableTaskListener) {
            try {
                ActivityTaskManager.getService().registerTaskStackListener(mTaskListener);
                WindowManagerGlobal.getWindowManagerService().watchRotation(mWatcher, mContext.getDisplayId());
            } catch (Exception e) {
                Log.e(TAG, "registerTaskStackListener failed");
            }
        }
    }

    public int getRotationId() {
        int rotation = mWindowManager.getDefaultDisplay().getRotation();
        switch (rotation) {
            case Surface.ROTATION_0:
            case Surface.ROTATION_180:
                rotation = 0;
                break;
            case Surface.ROTATION_90:
            case Surface.ROTATION_270:
                rotation = 1;
                break;
        }
        return rotation;
    }

    public void saveCoordinate(int[] left, int right[]) {
        saveCoordinate(mPackageUtils.getTopPackageName(), left, right);
    }

    public void saveCoordinate(String packageName, int[] left, int right[]) {
        if (getKeyMappingGlobalMode()) {
            packageName = GLOBAL_MODE_PACKAGE;
        }
        saveCoordinate(packageName, getRotationId(), left, right);
    }

    public void saveCoordinate(String packageName, int rotation, int[] left, int right[]) {
        ShoulderKeyInfo info = getAppInfo(packageName, rotation);
        if (info != null) {
            info.setAllCoordinate(new int[][]{left, right});            
        } else {
            info = new ShoulderKeyInfo(packageName, true, rotation, left, right);
            mInfoList.add(info);
        }
        notifyChanged();
    }

    public ShoulderKeyInfo getAppInfo(String packageName, int rotation) {
        for (ShoulderKeyInfo info : mInfoList) {
            if (info.getPackageName().equals(packageName) && info.getRotation() == rotation) {
                return info;
            }
        }
        return null;
    }

    public int[][] loadCoordinate(String packageName) {
        if (getKeyMappingGlobalMode()) {
            packageName = GLOBAL_MODE_PACKAGE;
        }
        return loadCoordinate(packageName, getRotationId());
    }

    public int[][] loadCoordinate(String packageName, int rotation) {
        int[][] coordinate = null;
        ShoulderKeyInfo info = getAppInfo(packageName, rotation);
        if (info != null) {
            coordinate = info.getAllCoordinate();
        }
        return coordinate;
    }

    public void setEnabledByPackage(String packageName, boolean enabled) {
        for (int i = 0; i < 2; i++) {
            ShoulderKeyInfo info = getAppInfo(packageName, i);
            if (info == null) {
                info = new ShoulderKeyInfo(packageName, enabled, i);
                mInfoList.add(info);
            } else {
                info.setEnabled(enabled);
            }
        }
        notifyChanged();
    }
    
    public int[][] loadCoordinateByTopPackage() {
        String packageName = mPackageUtils.getTopPackageName();
        int rotation = getRotationId();
        if (getKeyMappingGlobalMode()) {
            packageName = GLOBAL_MODE_PACKAGE;
        }
        ShoulderKeyInfo info = mDB.getAppKeyInfo(packageName, rotation);
        int[][] coordinate = null;
        if (info != null) {
            if (info.getEnabled()) {
                coordinate = info.getAllCoordinate();
            }
        }
        return coordinate;
    }

    public int[][] loadCoordinateByTaskPackage() {
        if (getKeyMappingGlobalMode()) {
            mPackageName = GLOBAL_MODE_PACKAGE;
        }
        ShoulderKeyInfo info = mDB.getAppKeyInfo(mPackageName, mRotation);
        int[][] coordinate = null;
        if (info != null) {
            if (info.getEnabled()) {
                coordinate = info.getAllCoordinate();
            }
        }
        return coordinate;
    }

    public boolean getEnabledByPackage(String packageName) {
        if (getKeyMappingGlobalMode()) {
            return true;
        }
        for (int i = 0; i < 2; i++) {
            ShoulderKeyInfo info = getAppInfo(packageName, i);
            if (info != null) {
                return info.getEnabled();
            }
        }
        return false;
    }

    public void saveAllAppsCoordinate() {
        for (ShoulderKeyInfo info : mInfoList) {
            mDB.saveAppKeyInfo(info);
        }
        mInfoList = mDB.getAllAppsKeyInfo();
    }

    public void resetAllAppsCoordinate() {
        mInfoList.clear();
        mDB.deleteAllAppsKeyInfo();
        notifyChanged();
    }

    public void notifyChanged() {
        if (mListener != null)
            mListener.onCoordinateChanged();
    }

    public boolean getKeyMappingGlobalMode() {
        return mDB.getKeyMappingGlobalMode();
    }

    public void setKeyMappingGlobalMode(boolean enable) {
        mDB.setKeyMappingGlobalMode(enable);
    }

}
