/*
 * Copyright (C) 2023 The LineageOS Project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.lineageos.settings.shoulderkey;

import java.util.Arrays;

public class ShoulderKeyInfo {
    private String mPackageName;
    private boolean mEnabled;
    private int mRotation;
    private int[] mLeftCoordinate = new int[2];
    private int[] mRightCoordinate = new int[2];

    public ShoulderKeyInfo() {
    }

    public ShoulderKeyInfo(ShoulderKeyInfo info) {
        mPackageName = info.getPackageName();
        mEnabled = info.getEnabled();
        mRotation = info.getRotation();
        mLeftCoordinate = info.getLeftCoordinate();
        mRightCoordinate = info.getRightCoordinate();
    }

    public ShoulderKeyInfo(String packageName, boolean enabled, int rotation, int[] left, int[] right) {
        mPackageName = packageName;
        mEnabled = enabled;
        mRotation = rotation;
        mLeftCoordinate = left;
        mRightCoordinate = right;
    }

    public ShoulderKeyInfo(String packageName, boolean enabled, int rotation) {
        mPackageName = packageName;
        mEnabled = enabled;
        mRotation = rotation;
    }

    public String getPackageName() {
        return mPackageName;
    }

    public void setPackageName(String name) {
        mPackageName = name;
    }

    public void setEnabled(int enabled) {
        mEnabled = enabled == 1;
    }

    public void setEnabled(boolean enabled) {
        mEnabled = enabled;
    }

    public boolean getEnabled() {
        return mEnabled;
    }

    public int getEnabledInt() {
        return mEnabled ? 1 : 0;
    }

    public void setRotation(int rotation) {
        mRotation = rotation;
    }

    public int getRotation() {
        return mRotation;
    }

    public int[] getLeftCoordinate() {
        return mLeftCoordinate;
    }

    public void setLeftCoordinate(int[] newCoordinate) {
        mLeftCoordinate = newCoordinate;
    }

    public int[] getRightCoordinate() {
        return mRightCoordinate;
    }

    public void setRightCoordinate(int[] newCoordinate) {
        mRightCoordinate = newCoordinate;
    }

    public void setAllCoordinate(int[][] newCoordinate) {
        mLeftCoordinate = newCoordinate[0];
        mRightCoordinate = newCoordinate[1];
    }

    public int[][] getAllCoordinate() {
        if (mLeftCoordinate[0] == 0 && mRightCoordinate[0] == 0)
            return null;
        int[][] coordinate = new int[][]{
            mLeftCoordinate,
            mRightCoordinate,
        };
        return coordinate;
    }

    public void setLeftX(int value) {
        mLeftCoordinate[0] = value;
    }

    public int getLeftX() {
       return mLeftCoordinate[0];
    }

    public void setLeftY(int value) {
        mLeftCoordinate[1] = value;        
    }

    public int getLeftY() {
       return mLeftCoordinate[1];
    }

    public void setRightX(int value) {
        mRightCoordinate[0] = value;        
    }

    public int getRightX() {
       return mRightCoordinate[0];
    }

    public void setRightY(int value) {
        mRightCoordinate[1] = value;        
    }

    public int getRightY() {
       return mRightCoordinate[1];
    }

    public String toString() {
        return ShoulderKeyInfo.class.getSimpleName() + ": package: " + mPackageName + ", enabled: " + mEnabled + ", rotation: " + mRotation + ", coordinate: " + Arrays.toString(mLeftCoordinate) + Arrays.toString(mRightCoordinate);
    }

}
