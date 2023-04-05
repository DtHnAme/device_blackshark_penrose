/*
 * Copyright (C) 2015 The CyanogenMod Project
 *               2017-2018 The LineageOS Project
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

import static org.lineageos.settings.shoulderkey.ShoulderKeyUtils.DEBUG;

import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.IBinder;
import android.os.UserHandle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.lineageos.settings.R;
import org.lineageos.settings.utils.SoundUtils;
import org.lineageos.settings.utils.PackageUtils;
import org.lineageos.settings.utils.NotificationUtils;

public class ShoulderKeyService extends Service {
    private static final String TAG = "ShoulderKeyService";

    public static Context mContext;

    public static ShoulderKeyService mShoulderKeyService;
    public static ShoulderKeyDBHelper mDataBase;
    public static ShoulderKeyUtils mUtils;
    public static PackageUtils mPackageUtils;    

    private WindowManager mWindowManager;
    private WindowManager.LayoutParams[] mParams;

    private ImageView mButtonL, mButtonR, mCancelView, mAppIcon;
    private TextView mAppName, mCenterText;
    private RelativeLayout mRelativeLayout;

    private int[] mViewSize;
    private int mRotation = 0;
    private boolean mGlobal = true;
    private boolean mShowing;
    private String mPackageName;

    private BroadcastReceiver mScreenStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF) && mRelativeLayout != null) {
                removeKeyMappingViews();
            }
        }
    };

    public ShoulderKeyService() {
        mShoulderKeyService = this;
    }

    public static ShoulderKeyService getInstance() {
        return mShoulderKeyService;
    }

    public Context getContext() {
        return mContext;
    }

    public static void startService(Context context) {
        if (mShoulderKeyService == null)
            mShoulderKeyService = new ShoulderKeyService();
        if (DEBUG) Log.i(TAG, "Starting service");
        context.startServiceAsUser(new Intent(context, ShoulderKeyService.class),
                UserHandle.CURRENT);
    }

    @Override
    public void onCreate() {
        if (DEBUG) Log.i(TAG, "Creating service");

        ShoulderKeyManager.getInstance().register(this);
        SoundUtils.init(this);
        NotificationUtils.getInstance().initialize(this);
        mContext = this;
        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        mDataBase = new ShoulderKeyDBHelper(this);
        mPackageUtils = new PackageUtils(this);
        mUtils = new ShoulderKeyUtils(mDataBase, this);

        IntentFilter screenStateFilter = new IntentFilter();
        screenStateFilter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mScreenStateReceiver, screenStateFilter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (DEBUG) Log.i(TAG, "Starting service");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (DEBUG) Log.i(TAG, "Destroying service");
        super.onDestroy();
        ShoulderKeyManager.getInstance().unregister();
        SoundUtils.release();
        mWindowManager = null;
        mShoulderKeyService = null;
        mDataBase = null;
        mPackageUtils = null;
        mUtils = null;
        unregisterReceiver(mScreenStateReceiver);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public ShoulderKeyUtils getShoulderKeyUtils() {
        return mUtils;
    }
    
    public void showKeyMappingViews() {
        showKeyMappingViews(getTopPackageName(), 0, true);
    }

    public void showKeyMappingViews(String packageName, int rotation, boolean global) {
        if (mRelativeLayout != null) {
            removeKeyMappingViews();
            return;
        }
        mPackageName = packageName;
        mRotation = rotation;
        mGlobal = global;
        
        if (!mUtils.getEnabledByPackage(packageName)) {
            Toast.makeText(mContext, mContext.getString(R.string.shoulder_key_app_disable), Toast.LENGTH_SHORT).show();
            return;
        }
        
        loadParams();
        createView();
        createButtonView();
        updateLocation();
        mShowing = true;
    }

    public void removeKeyMappingViews() {
        mWindowManager.removeView(mRelativeLayout);
        mWindowManager.removeView(mButtonL);
        mWindowManager.removeView(mButtonR);
        mRelativeLayout = null;
        mButtonL = null;
        mButtonR = null;
        mShowing = false;
        mUtils.saveAllAppsCoordinate();
    }

    private void loadParams() {
        mViewSize = new int[]{100, 100};
        mParams = new WindowManager.LayoutParams[3];
        for (int i = 0; i < mParams.length; i++) {
            mParams[i] = new WindowManager.LayoutParams();
            if (i == 2) {
                mParams[i].flags =
                                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH |
                                WindowManager.LayoutParams.FLAG_DIM_BEHIND |
                                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
                mParams[i].dimAmount = 0.5f;
                mParams[i].type =
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
                mParams[i].alpha = 100;
                continue;
            }
            mParams[i].height = mViewSize[0];
            mParams[i].width = mViewSize[1];
            mParams[i].flags =
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH |
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
            mParams[i].type =
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            mParams[i].alpha = 100;
        }
    }

    public void updateLocation() {
        int[][]  coordinate = mUtils.loadCoordinate(mPackageName);
        Rect rect = mWindowManager.getCurrentWindowMetrics().getBounds();

        for (int i = 0; i < mParams.length; i++) {
            if (i == 2) continue;
            if (coordinate == null) continue;
            mParams[i].x = coordinate[i][0] - rect.centerX();
            mParams[i].y += coordinate[i][1] - rect.centerY();
        }

        mWindowManager.updateViewLayout(mButtonL, mParams[0]);
        mWindowManager.updateViewLayout(mButtonR, mParams[1]);
    }

    private void createView() {
        mRelativeLayout = new RelativeLayout(mContext);
        mCancelView = new ImageView(mContext);
        mAppIcon = new ImageView(mContext);
        mAppName = new TextView(mContext);
        mCenterText = new TextView(mContext);

        mCancelView.setImageResource(R.drawable.ic_close);
        mAppIcon.setImageDrawable(mPackageUtils.getAppIcon(mPackageName));
        mAppIcon.setPadding(5, 5, 5, 5);
        mAppName.setId(View.generateViewId());
        mAppName.setTextSize(20f);
        mAppName.setGravity(Gravity.CENTER_VERTICAL);
        mAppName.setTextColor(0xffaaaaaa);
        mAppName.setText(mPackageUtils.getAppName(mPackageName));
        if (mUtils.getKeyMappingGlobalMode() && mGlobal) {
            mAppName.setText(R.string.key_mapping_global_mode);
            mAppIcon.setImageResource(R.drawable.ic_shoulderkey);
            mAppIcon.setImageTintList(ColorStateList.valueOf(0xffaaaaaa));
        }
        mCenterText.setText(R.string.key_mapping_long_press);
        mCenterText.setTextColor(0xffaaaaaa);

        mRelativeLayout.setClickable(false);
        mRelativeLayout.setFocusableInTouchMode(true);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(mViewSize[0], mViewSize[1]);
        params.topMargin = 100;
        params.setMarginEnd(20);
        params.alignWithParent = true;
        params.addRule(RelativeLayout.LEFT_OF, mAppName.getId());
        mRelativeLayout.addView(mAppIcon, params);

        params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, mViewSize[1]);
        params.topMargin = 100;
        params.alignWithParent = true;
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        mRelativeLayout.addView(mAppName, params);

        params = new RelativeLayout.LayoutParams(mViewSize[0], mViewSize[1]);
        params.topMargin = 100;
        params.setMarginStart(20);
        params.alignWithParent = true;
        params.addRule(RelativeLayout.RIGHT_OF, mAppName.getId());
        mRelativeLayout.addView(mCancelView, params);

        params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.alignWithParent = true;
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        mRelativeLayout.addView(mCenterText, params);

        mWindowManager.addView(mRelativeLayout, mParams[2]);
        mCancelView.setOnClickListener(v -> {
            removeKeyMappingViews();
        });
        mRelativeLayout.setOnLongClickListener(v -> {
            removeKeyMappingViews();
            return true;
        });
    }
    
    private void createButtonView() {
        mButtonL = new ImageView(mContext);
        mButtonR = new ImageView(mContext);
        mButtonL.setImageResource(R.drawable.ic_shoulder_key_left);
        mButtonR.setImageResource(R.drawable.ic_shoulder_key_right);

        mWindowManager.addView(mButtonL, mParams[0]);
        mWindowManager.addView(mButtonR, mParams[1]);

        mButtonL.setOnTouchListener(new TouchListener());
        mButtonR.setOnTouchListener(new TouchListener());

        mButtonR.setFocusableInTouchMode(true);
        mButtonR.requestFocus();
        mButtonR.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        removeKeyMappingViews();
                        return true;
                    }
                }
                return false;
            }
        });
    }

    private void saveCoordinate() {
        int[] leftLocation = new int[2];
        int[] rightLocation = new int[2];
        mButtonL.getLocationOnScreen(leftLocation);
        mButtonR.getLocationOnScreen(rightLocation);
        int[] leftKey = new int[]{leftLocation[0] + (mViewSize[0] / 2), leftLocation[1] + (mViewSize[1] / 2)};
        int[] rightKey = new int[]{rightLocation[0] + (mViewSize[0] / 2), rightLocation[1] + (mViewSize[1] / 2)};
        mUtils.saveCoordinate(mPackageName, leftKey, rightKey);
    }

    public String getTopPackageName() {
       return mPackageUtils.getTopPackageName();
    }

    public boolean getKeyMappingGlobalMode() {
        return mUtils.getKeyMappingGlobalMode();
    }

    public void setKeyMappingGlobalMode(boolean enable) {
        mUtils.setKeyMappingGlobalMode(enable);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mShowing) {
            removeKeyMappingViews();
            showKeyMappingViews();
        }
    }

    private class TouchListener implements View.OnTouchListener {
        private float startX, startY;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startX = event.getRawX();
                    startY = event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    float nowX = event.getRawX();
                    float nowY = event.getRawY();
                    float movedX = nowX - startX;
                    float movedY = nowY - startY;
                    startX = nowX;
                    startY = nowY;

                    WindowManager.LayoutParams params = (WindowManager.LayoutParams) v.getLayoutParams();
                    params.x += (int) movedX;
                    params.y += (int) movedY;

                    mWindowManager.updateViewLayout(v, params);
                    mRelativeLayout.setTransitionAlpha(0f);
                    break;
                case MotionEvent.ACTION_UP:
                    mRelativeLayout.setTransitionAlpha(1f);
                    saveCoordinate();
                    break;
            }
            return false;
        }
    }

}
