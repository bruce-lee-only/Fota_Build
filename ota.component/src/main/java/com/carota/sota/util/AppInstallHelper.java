/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/
package com.carota.sota.util;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;

import com.momock.util.FileHelper;
import com.momock.util.Logger;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicInteger;

public class AppInstallHelper {
    public static final int INSTALL_COMPLETE = 1;
    public static final int CREATE_SESSION_ERROR = 2;
    public static final int COPY_ERROR = 3;
    public static final int EXEC_INSTALL_ERROR = 4;
    public static final int DOWNLOAD_ERROR = 5;
    public static final int MD5_ERROR = 6;

    public static int install(Context context, String apkFilePath, PackageManager packageManager) {
        File apkFile = new File(apkFilePath);
        PackageInstaller packageInstaller = packageManager.getPackageInstaller();
        PackageInstaller.SessionParams sessionParams = new PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL);
        sessionParams.setSize(apkFile.length());

        int sessionId = createSession(packageInstaller, sessionParams);
        Logger.debug("sessionId = " + sessionId);
        if (sessionId != -1) {
            boolean copySuccess = copyInstallFile(packageInstaller, sessionId, apkFilePath);
            Logger.debug("copySuccess = " + copySuccess);
            if (copySuccess) {
                return execInstallCommand(context, packageInstaller, sessionId);
            } else {
                return COPY_ERROR;
            }
        } else {
            return CREATE_SESSION_ERROR;
        }
    }

    private static int createSession(PackageInstaller packageInstaller,
                                     PackageInstaller.SessionParams sessionParams) {
        int sessionId = -1;
        try {
            sessionId = packageInstaller.createSession(sessionParams);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sessionId;
    }

    private static boolean copyInstallFile(PackageInstaller packageInstaller,
                                           int sessionId, String apkFilePath) {
        boolean success = false;
        File apkFile = new File(apkFilePath);
        try (
            InputStream in = new FileInputStream(apkFile);
            PackageInstaller.Session session = packageInstaller.openSession(sessionId);
            OutputStream out = session.openWrite("base.apk", 0, apkFile.length())){

            FileHelper.copy(in, out);
            session.fsync(out);
            success = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return success;
    }

    private static int execInstallCommand(Context context, PackageInstaller packageInstaller, int sessionId) {
        PackageInstaller.Session session = null;
        AtomicInteger installState = new AtomicInteger(-2);
        try {
            session = packageInstaller.openSession(sessionId);
            BroadcastReceiver receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Logger.debug("onReceive intent =  "+ intent);
                    if (intent != null) {
                        int status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS,PackageInstaller.STATUS_FAILURE);
                        String packageName = intent.getStringExtra(PackageInstaller.EXTRA_PACKAGE_NAME);
                        Logger.debug("onReceive packageName = " + packageName + " / status = " + status);
                        installState.set(status);
                        context.unregisterReceiver(this);
                    }
                }
            };
            IntentFilter intentFilter = new IntentFilter("android.content.pm.extra.STATUS");
            context.registerReceiver(receiver, intentFilter);
            Intent intent = new Intent("android.content.pm.extra.STATUS");
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            session.commit(pendingIntent.getIntentSender());
            long timeout = 1000 * 60 * 5;
            long targetTime = System.currentTimeMillis() + timeout;
            long curTime;
            do {
                try {
                    Thread.sleep(1000 * 5);
                } catch (InterruptedException e) {
                    // do nothing
                }
                Logger.debug("waitInstallFinished sessionId = " + sessionId + " / state = " + installState.get());
                curTime = System.currentTimeMillis();
                if (curTime >= targetTime) {
                    break;
                }
            } while (installState.get() == -2);
            if (installState.get() == PackageInstaller.STATUS_SUCCESS) {
                return INSTALL_COMPLETE;
            }
        } catch (IOException e) {
            e.printStackTrace();
            Logger.error("IOException = " + e.getMessage());
        } finally {
            closeQuietly(session);
        }
        return EXEC_INSTALL_ERROR;
    }

    private static void closeQuietly(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException ignored) {
                ignored.printStackTrace();
            }
        }
    }
}
