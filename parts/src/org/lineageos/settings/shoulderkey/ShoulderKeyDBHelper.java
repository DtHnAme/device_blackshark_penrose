/*
 * Copyright (C) 2023 The LineageOS Project
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.settings.shoulderkey;

import static org.lineageos.settings.shoulderkey.ShoulderKeyUtils.DEBUG;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

public class ShoulderKeyDBHelper extends SQLiteOpenHelper {
    private static final String TAG = "ShoulderKeyDBHelper";
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "shoulderKey.db";
    private static final String SYSTEM_DB_PATH = "/data/system/" + DATABASE_NAME;

    public static class KeyMappingEntry implements BaseColumns {
        public static final String TABLE_NAME = "KeyMapping";
        public static final String PACKAGE_NAME = "package_name";
        public static final String ENABLED = "enabled";
        public static final String ROTATION = "rotation";
        public static final String LEFT_KEY_X = "left_x";
        public static final String LEFT_KEY_Y = "left_y";
        public static final String RIGHT_KEY_X = "right_x";
        public static final String RIGHT_KEY_Y = "right_y";
    }

    public static class ConfigEntry implements BaseColumns {
        public static final String TABLE_NAME = "KeyMappingConfig";
        public static final String GLOBAL_MODE = "global_mode";
    }

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE IF NOT EXISTS " + KeyMappingEntry.TABLE_NAME + " (" +
                    KeyMappingEntry._ID + " INTEGER PRIMARY KEY," +
                    KeyMappingEntry.PACKAGE_NAME + " TEXT," +
                    KeyMappingEntry.ENABLED + " INTEGER," +
                    KeyMappingEntry.ROTATION + " INTEGER," +
                    KeyMappingEntry.LEFT_KEY_X + " INTEGER," +
                    KeyMappingEntry.LEFT_KEY_Y + " INTEGER," +
                    KeyMappingEntry.RIGHT_KEY_X + " INTEGER," +
                    KeyMappingEntry.RIGHT_KEY_Y + " INTEGER)";

    private static final String SQL_CONFIG_CREATE_ENTRIES =
            "CREATE TABLE IF NOT EXISTS " + ConfigEntry.TABLE_NAME + " (" +
                    ConfigEntry._ID + " INTEGER PRIMARY KEY," +
                    ConfigEntry.GLOBAL_MODE + " INTEGER)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + KeyMappingEntry.TABLE_NAME;

    public ShoulderKeyDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
        db.execSQL(SQL_CONFIG_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public SQLiteDatabase getSystemSholuderKeyDB() {
        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(SYSTEM_DB_PATH, null);
        db.execSQL(SQL_CREATE_ENTRIES);
        db.execSQL(SQL_CONFIG_CREATE_ENTRIES);
        return db;
    }

    public void addAppKeyInfo(ShoulderKeyInfo info) {
        SQLiteDatabase db = getSystemSholuderKeyDB();
        ContentValues values = new ContentValues();
        values.put(KeyMappingEntry.PACKAGE_NAME, info.getPackageName());
        values.put(KeyMappingEntry.ENABLED, info.getEnabled());
        values.put(KeyMappingEntry.ROTATION, info.getRotation());
        values.put(KeyMappingEntry.LEFT_KEY_X, info.getLeftX());
        values.put(KeyMappingEntry.LEFT_KEY_Y, info.getLeftY());
        values.put(KeyMappingEntry.RIGHT_KEY_X, info.getRightX());
        values.put(KeyMappingEntry.RIGHT_KEY_Y, info.getRightY());
        db.insertWithOnConflict(KeyMappingEntry.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void updateAppKeyInfo(ShoulderKeyInfo info) {
        SQLiteDatabase db = getSystemSholuderKeyDB();
        String selection = KeyMappingEntry.PACKAGE_NAME + "=?" + " and " + KeyMappingEntry.ROTATION + "=?";
        String[] selectionArgs = {info.getPackageName(), String.valueOf(info.getRotation())};

        ContentValues values = new ContentValues();
        values.put(KeyMappingEntry.ENABLED, info.getEnabledInt());
        values.put(KeyMappingEntry.LEFT_KEY_X, info.getLeftX());
        values.put(KeyMappingEntry.LEFT_KEY_Y, info.getLeftY());
        values.put(KeyMappingEntry.RIGHT_KEY_X, info.getRightX());
        values.put(KeyMappingEntry.RIGHT_KEY_Y, info.getRightY());
        db.update(KeyMappingEntry.TABLE_NAME, values, selection, selectionArgs);
    }

    public void saveAppKeyInfo(ShoulderKeyInfo info) {
        if (getAppKeyInfo(info.getPackageName(), info.getRotation()) != null) {
            updateAppKeyInfo(info);
        } else {
            addAppKeyInfo(info);
        }
    }

    public void deleteAllAppsKeyInfo() {
        SQLiteDatabase db = getSystemSholuderKeyDB();
        db.delete(KeyMappingEntry.TABLE_NAME, null, null);        
        db.close();
    }

    public void deleteAppKeyInfo(ShoulderKeyInfo info) {
        SQLiteDatabase db = getSystemSholuderKeyDB();
        String selection = KeyMappingEntry.PACKAGE_NAME + "=?" + " and " + KeyMappingEntry.ROTATION + "=?";
        String[] selectionArgs = {info.getPackageName(), String.valueOf(info.getRotation())};
        
        db.delete(KeyMappingEntry.TABLE_NAME, selection, selectionArgs);        
        db.close();
    }

    public List<ShoulderKeyInfo> getAllAppsKeyInfo() {
        return getAppsKeyInfo(null, null);
    }

    public ShoulderKeyInfo getAppKeyInfo(String packageName, int rotation) {
        String selection = KeyMappingEntry.PACKAGE_NAME + "=?" + " and " + KeyMappingEntry.ROTATION + "=?";
        String[] selectionArgs = {packageName, String.valueOf(rotation)};
        for (ShoulderKeyInfo info : getAppsKeyInfo(selection, selectionArgs)) {
            return info;
        }
        return null;
    }

    public List<ShoulderKeyInfo> getAppsKeyInfo(String selection, String[] selectionArgs) {
        SQLiteDatabase db = getSystemSholuderKeyDB();
        String[] projection = {
                KeyMappingEntry.PACKAGE_NAME,
                KeyMappingEntry.ENABLED,
                KeyMappingEntry.ROTATION,
                KeyMappingEntry.LEFT_KEY_X,
                KeyMappingEntry.LEFT_KEY_Y,
                KeyMappingEntry.RIGHT_KEY_X,
                KeyMappingEntry.RIGHT_KEY_Y,
        };
        Cursor cursor = db.query(KeyMappingEntry.TABLE_NAME, projection, selection, selectionArgs,
                null, null, null);

        List<ShoulderKeyInfo> infos = new ArrayList<>();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                ShoulderKeyInfo info = new ShoulderKeyInfo();
                int index = cursor.getColumnIndex(KeyMappingEntry.PACKAGE_NAME);
                info.setPackageName(cursor.getString(index));
                index = cursor.getColumnIndex(KeyMappingEntry.ENABLED);
                info.setEnabled(cursor.getInt(index));
                index = cursor.getColumnIndex(KeyMappingEntry.ROTATION);
                info.setRotation(cursor.getInt(index));
                index = cursor.getColumnIndex(KeyMappingEntry.LEFT_KEY_X);
                info.setLeftX(cursor.getInt(index));
                index = cursor.getColumnIndex(KeyMappingEntry.LEFT_KEY_Y);
                info.setLeftY(cursor.getInt(index));
                index = cursor.getColumnIndex(KeyMappingEntry.RIGHT_KEY_X);
                info.setRightX(cursor.getInt(index));
                index = cursor.getColumnIndex(KeyMappingEntry.RIGHT_KEY_Y);
                info.setRightY(cursor.getInt(index));
                infos.add(info);
            }
            cursor.close();
        }
        db.close();
        if (DEBUG) Log.i(TAG, "getAppsKeyInfo " + infos.toString());
        return infos;
    }

    public boolean getKeyMappingGlobalMode() {
        int enable = -1;
        SQLiteDatabase db = getSystemSholuderKeyDB();
        String[] projection = {ConfigEntry.GLOBAL_MODE};
        String selection = ConfigEntry._ID + "=?";
        String[] selectionArgs = {"1"};
        Cursor cursor = db.query(ConfigEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, null);
        
        if (cursor != null) {
            while (cursor.moveToNext()) {
                int index = cursor.getColumnIndex(ConfigEntry.GLOBAL_MODE);
                enable = cursor.getInt(index);
            }
            cursor.close();
        } 
        
        if (enable == -1) {
            ContentValues values = new ContentValues();
            values.put(ConfigEntry.GLOBAL_MODE, 0);
            db.insertWithOnConflict(ConfigEntry.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        }

        db.close();
        
        return enable == 1;
    }

    public void setKeyMappingGlobalMode(boolean enable) {
        SQLiteDatabase db = getSystemSholuderKeyDB();
        ContentValues values = new ContentValues();
        values.put(ConfigEntry.GLOBAL_MODE, enable ? 1 : 0);
        db.update(ConfigEntry.TABLE_NAME, values, null, null);
    }

}
