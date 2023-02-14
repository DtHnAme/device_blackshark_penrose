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

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.UserHandle;
import android.util.Log;

import org.lineageos.settings.utils.SoundUtils;

public class ShoulderKeyService extends Service {
    private static final String TAG = "ShoulderKeyService";

    public static void startService(Context context) {
        Log.i(TAG, "Starting service");
        context.startServiceAsUser(new Intent(context, ShoulderKeyService.class),
                UserHandle.CURRENT);
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "Creating service");

        ShoulderKeyManager.getInstance().register(this);
        SoundUtils.init(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Starting service");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "Destroying service");
        super.onDestroy();
        ShoulderKeyManager.getInstance().unregister();
        SoundUtils.release();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
