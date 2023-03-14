/*
 * Copyright (C) 2023 The LineageOS Project
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.settings.shoulderkey;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.util.Slog;
import android.view.InputDevice;
import android.view.InputEvent;
import android.view.InputFilter;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.WindowManagerPolicyConstants;

import org.lineageos.settings.utils.PackageUtils;

import java.util.Arrays;
import java.util.ArrayList;

public class ShoulderKeyInputFiltrer extends InputFilter {
    private static final String TAG = ShoulderKeyInputFiltrer.class.getSimpleName();
    private static final boolean DEBUG = ShoulderKeyUtils.DEBUG;

    private static final int INVALID_POINTER_ID = -1;
    private static final int BS_SHOULDERKEY_ID = 1;
    private static final int LEFT_KEY_CODE = 131;
    private static final int RIGHT_KEY_CODE = 132;
    private static final int MAX_POINTER_COUNT = 16;

    private ShoulderKeyDBHelper mDataBase;
    private ShoulderKeyUtils mUtils;

    private Pos[] mTouchList;
    private Context mContext;

    public ShoulderKeyInputFiltrer(Context context) {
        super(context.getMainLooper());
        
        mTouchList = new Pos[16];
        for (int i = 0; i < 16; i++) {
            mTouchList[i] = new Pos();
        }
        mContext = context;
        mDataBase = new ShoulderKeyDBHelper(context);
        mUtils = new ShoulderKeyUtils(mDataBase, context, true);
    }

    @Override
    public void onInstalled() {
        if (DEBUG) Slog.d(TAG, "ShoulderKeyInputFiltrer input filter installed.");
        super.onInstalled();
    }

    @Override
    public void onUninstalled() {
        if (DEBUG) Slog.d(TAG, "ShoulderKeyInputFiltrer input filter uninstalled.");
        super.onUninstalled();
    }

    @Override
    public void onInputEvent(InputEvent event, int policyFlags) {
        if (DEBUG) Slog.d(TAG, "Received event: " + event + ", policyFlags=0x"
                + Integer.toHexString(policyFlags));

        if (!processInputEvent(event, policyFlags))
            super.onInputEvent(event, policyFlags);
    }

    private boolean processInputEvent(InputEvent event, int policyFlags) {
        if ((event.getSource() & InputDevice.SOURCE_KEYBOARD) == InputDevice.SOURCE_KEYBOARD)
            return processShoulderKeyEvent((KeyEvent) event);
        
        if ((event.getSource() & InputDevice.SOURCE_TOUCHSCREEN) == InputDevice.SOURCE_TOUCHSCREEN) {
            return processTouchScreenEvent((MotionEvent) event);        
        }
        return false;
    }

    private boolean processShoulderKeyEvent(KeyEvent event) {
        if (DEBUG) Slog.d(TAG, "KeyEvent: " + event.getKeyCode() + " ProductId: " + event.getDevice().getProductId() + " VendorId: " + event.getDevice().getVendorId());

        if (!(event.getDevice().getProductId() == BS_SHOULDERKEY_ID && event.getDevice().getVendorId() == BS_SHOULDERKEY_ID))
            return false;

        int keycode = event.getKeyCode();
        if (!(keycode == LEFT_KEY_CODE || keycode == RIGHT_KEY_CODE))
            return false;

        int action = event.getAction();
        int touchId = 11;

        boolean isLeft = keycode == LEFT_KEY_CODE;

        int[][] coordinate = mUtils.loadCoordinateByTaskPackage();

        if (action == KeyEvent.ACTION_DOWN || action == KeyEvent.ACTION_UP) {
            if (coordinate == null)
                return false;
            Pos pos = new Pos(coordinate[1][0], coordinate[1][1]);
            if (isLeft) {
                pos = new Pos(coordinate[0][0], coordinate[0][1]);
            }
            pos.setTouchId(isLeft ? touchId : touchId + 1);
            return startTouchInput(buildMotionEvent(insertTouch(pos), action));
        }

        return false;
    }

    private boolean processTouchScreenEvent(MotionEvent event) {
        int pointerCount = event.getPointerCount();
        int actionIndex = INVALID_POINTER_ID;
        for (int i = 0; i < pointerCount; i++) {
            Pos pos = new Pos(event.getX(i), event.getY(i));
            MotionEvent.PointerCoords pointerCoords = new MotionEvent.PointerCoords();
            event.getPointerCoords(i, pointerCoords);
            pos.setPointerCoords(pointerCoords);
            pos.setTouchId(event.getPointerId(i) + 1);
            if (i == event.getActionIndex()) {
                actionIndex = insertTouch(pos);
            } else {
                insertTouch(pos);
            }
        }
        return startTouchInput(buildMotionEvent(actionIndex, event.getAction() & MotionEvent.ACTION_MASK));
    }
    
    private boolean startTouchInput(MotionEvent event) {
        if (event == null)
            return false;
        int policyFlags = WindowManagerPolicyConstants.FLAG_TRUSTED | WindowManagerPolicyConstants.FLAG_FILTERED |
                          WindowManagerPolicyConstants.FLAG_INTERACTIVE | WindowManagerPolicyConstants.FLAG_PASS_TO_USER;
        sendInputEvent(event, policyFlags);
        if (DEBUG) Slog.d(TAG, "startTouchInput event = " + event.toString() + ", policyFlags=0x" + Integer.toHexString(policyFlags));
        return true;
    }

    private MotionEvent buildMotionEvent(int actionIndex, int action) {
        return buildMotionEvent(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), actionIndex, action);
    }
    
    private MotionEvent buildMotionEvent(long downTime, long eventTime, int actionIndex, int action) {
        int pointerCount = 0;
        ArrayList<MotionEvent.PointerCoords> pointerCoordsList = new ArrayList<>();
        ArrayList<MotionEvent.PointerProperties> pointerPropsList = new ArrayList<>();
        for (int i = 0; i < MAX_POINTER_COUNT; i++) {
            if (mTouchList[i].isValid()) {
                MotionEvent.PointerProperties pointerProps = new MotionEvent.PointerProperties();
                pointerProps.id = i;
                pointerProps.toolType = MotionEvent.TOOL_TYPE_FINGER;
                pointerPropsList.add(pointerProps);
                MotionEvent.PointerCoords pointerCoords = new MotionEvent.PointerCoords();
                pointerCoords.copyFrom(mTouchList[i].getPointerCoords());
                pointerCoordsList.add(pointerCoords);
                pointerCount++;
            }
        }
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP) {
            removeTouch(actionIndex);
        }
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_HOVER_EXIT) {
            resetTouch();
        }
        if (pointerCount != 0) {
            if (!(pointerCount <= 1 || action == MotionEvent.ACTION_MOVE || action == MotionEvent.ACTION_CANCEL)) {
                if (action == KeyEvent.ACTION_DOWN) {
                    action = MotionEvent.ACTION_POINTER_DOWN;
                } else if (action == KeyEvent.ACTION_UP) {
                    action = MotionEvent.ACTION_POINTER_UP;
                }
                action |= actionIndex << MotionEvent.ACTION_POINTER_INDEX_SHIFT;
            }
            return MotionEvent.obtain(
                downTime, 
                eventTime, 
                action,
                pointerCount, 
                (MotionEvent.PointerProperties[]) pointerPropsList.toArray(new MotionEvent.PointerProperties[pointerPropsList.size()]), 
                (MotionEvent.PointerCoords[]) pointerCoordsList.toArray(new MotionEvent.PointerCoords[pointerCoordsList.size()]), 
                0, 0, 1.0f, 1.0f, 0, 0, InputDevice.SOURCE_TOUCHSCREEN, 0);
        } else {
            return null;
        }      
    }

    private int insertTouch(Pos pos) {
        int index = INVALID_POINTER_ID;
        for (int i = 0; i < MAX_POINTER_COUNT; i++) {
            if (mTouchList[i].isValid()) {
                index++;
                if (mTouchList[i].getTouchId() == pos.getTouchId()) {
                    mTouchList[i].setPos(pos);
                    return index;
                }
            }
        }

        for (int i2 = 0; i2 < MAX_POINTER_COUNT; i2++) {
            if (!mTouchList[i2].isValid()) {
                mTouchList[i2].setPos(pos);
                return i2;
            }
        }
        return INVALID_POINTER_ID;
    }

    private boolean removeTouch(int actionIndex) {
        if (actionIndex < 0 || actionIndex >= MAX_POINTER_COUNT) {
            return false;
        }
        for (int i = 0; i < MAX_POINTER_COUNT; i++) {
            if (mTouchList[i].isValid()) {
                if (actionIndex == 0) {
                    mTouchList[i].reset();
                    return true;
                }
                actionIndex--;
            }
        }
        return true;
    }

    private void resetTouch() {
        for (int i = 0; i < 16; i++) {
            if (mTouchList[i].isValid()) {
                mTouchList[i].reset();
            }
        }
    }

    public class Pos {
        private MotionEvent.PointerCoords pointerCoords;
        private int touchId;

        public Pos() {
            pointerCoords = new MotionEvent.PointerCoords();
            reset();
        }

        public Pos(float x, float y) {
            pointerCoords = new MotionEvent.PointerCoords();
            touchId = INVALID_POINTER_ID;
            pointerCoords.x = x;
            pointerCoords.y = y;
        }

        public void reset() {
            touchId = INVALID_POINTER_ID;
            pointerCoords.clear();
        }

        public boolean isValid() {
            return touchId != INVALID_POINTER_ID;
        }

        public void setX(float x) {
            pointerCoords.x = x;
        }

        public void setY(float y) {
            pointerCoords.y = y;
        }

        public void setTouchId(int newTouchId) {
            touchId = newTouchId;
        }

        public void setPointerCoords(MotionEvent.PointerCoords newPointerCoords) {
            pointerCoords = newPointerCoords;
        }

        public MotionEvent.PointerCoords getPointerCoords() {
            return pointerCoords;
        }

        public void setPos(Pos pos) {
            touchId = pos.getTouchId();
            pointerCoords = pos.getPointerCoords();
        }

        public float getX() {
            return pointerCoords.x;
        }

        public float getY() {
            return pointerCoords.y;
        }

        public int getTouchId() {
            return touchId;
        }

        public String toString() {
            return String.format("Pos {pointerCoords = %s, touchId = %d}", pointerCoords, Integer.valueOf(touchId));
        }
    }
}
