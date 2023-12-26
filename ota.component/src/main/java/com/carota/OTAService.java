/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import com.momock.util.SystemHelper;

public abstract class OTAService extends Service {

    public static void startService(Context context, Intent intent) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && shouldKeepAlive(context)) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    private static boolean shouldKeepAlive(Context context) {
        return SystemHelper.getAppMeta(context, "carota.keep.alive", true);
    }

    private static final String NOTIFY_CHANNEL_ID = "OTA_CORE";
    private boolean mKeepAlive;

    @Override
    public void onCreate() {
        super.onCreate();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notiChannel = new NotificationChannel(
                    NOTIFY_CHANNEL_ID, "CORE", NotificationManager.IMPORTANCE_NONE);
            notiChannel.setShowBadge(false);
            NotificationManager notiMgr = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
            notiMgr.createNotificationChannel(notiChannel);
        }

        mKeepAlive = shouldKeepAlive(this);
        if(mKeepAlive) {
            Notification noti;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                noti = new Notification.Builder(this, NOTIFY_CHANNEL_ID)
                        .setSmallIcon(android.R.drawable.sym_def_app_icon).build();
            } else {
                noti = new Notification.Builder(this).build();
            }
            startForeground(1, noti);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        if(mKeepAlive) {
            stopForeground(true);
        }
        super.onDestroy();
    }
}
