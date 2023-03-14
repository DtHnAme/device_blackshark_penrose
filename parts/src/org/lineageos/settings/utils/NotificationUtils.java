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

import static org.lineageos.settings.shoulderkey.ShoulderKeyFragment.PREVENT_ACCIDENTAL_KEY;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.util.Log;
import androidx.preference.PreferenceManager;

import org.lineageos.settings.R;
import org.lineageos.settings.shoulderkey.ShoulderKeyActivity;

public final class NotificationUtils {
    private static final String TAG = "NotificationUtils";

    private static final String CHANNEL_ID = "shoulder_key_state";
    private static final int NOTIFICATION_ID = 1;

    private static NotificationUtils mNotificationUtils;
    private static Context mContext;
    private static boolean leftEnable;
    private static boolean rightEnable;
    private static boolean isShowing;

    private NotificationManager mNotificationManager;
    private Notification mNotification;

    private NotificationUtils() {
        // This class is not supposed to be instantiated
    }

    public static NotificationUtils getInstance() {
        if (mNotificationUtils == null)
            mNotificationUtils = new NotificationUtils();
        return mNotificationUtils;
    }

    public void initialize(Context context) {
        mContext = context;
        mNotificationManager = mContext.getSystemService(NotificationManager.class);

        NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID,
                mContext.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH);
        notificationChannel.enableVibration(false);
        notificationChannel.setSound(null, null);
        mNotificationManager.createNotificationChannel(notificationChannel);

        Notification.Builder builder = new Notification.Builder(mContext, CHANNEL_ID);
        builder.setContentTitle(mContext.getString(R.string.notification_title));
        builder.setContentText(mContext.getText(R.string.notification_text));
        builder.setContentIntent(PendingIntent.getActivity(mContext, 0, new Intent(mContext, ShoulderKeyActivity.class), 0));
        builder.setSmallIcon(R.drawable.ic_shoulderkey);
        mNotification = builder.build();
        mNotification.flags |= Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
        mNotification.visibility = Notification.VISIBILITY_PUBLIC;
    }

    public void showNotification(boolean isLeft, boolean smartSwitchState) {
        if (isLeft)
            leftEnable = smartSwitchState;
        else
            rightEnable = smartSwitchState;
        
        if (leftEnable || rightEnable) {
            if (isPreventAccidentalEnabled())
                mNotificationManager.notify(NOTIFICATION_ID, mNotification);
        } else {
            mNotificationManager.cancel(NOTIFICATION_ID);
        }
    }

    public void removeNotification() {
        mNotificationManager.cancel(NOTIFICATION_ID);
    }

    private boolean isPreventAccidentalEnabled() {
        return PreferenceManager.getDefaultSharedPreferences(mContext)
                .getBoolean(PREVENT_ACCIDENTAL_KEY, false);
    }

}
