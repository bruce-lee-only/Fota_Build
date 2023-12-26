/*
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 */
package com.carota.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.SystemProperties;

import com.momock.util.Logger;

public class CrashHandler implements Thread.UncaughtExceptionHandler{
    private final Context mContext;
    private final Thread.UncaughtExceptionHandler mHandler;
    private CrashHandler(Context context) {
        mContext = context.getApplicationContext();
        mHandler = Thread.getDefaultUncaughtExceptionHandler();
    }

    public static void register(Context context) {
        Thread.setDefaultUncaughtExceptionHandler(new CrashHandler(context));
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        Logger.error("Fatal exception, thread = " + t.toString());
        StringBuilder builder = new StringBuilder();
        try {
            PackageInfo info = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            builder.append("package name = ").append(info.packageName);
            builder.append("\nversion name = ").append(info.versionName);
            builder.append("\nversion code = ").append(info.versionCode);
        } catch (PackageManager.NameNotFoundException ex) {
            Logger.error(ex);
        }
        builder.append("\nsystem description = ").append(SystemProperties.get("ro.build.description"));
        builder.append("\nsystem build date = ").append(SystemProperties.get("ro.build.date"));
        Logger.error(builder.toString());
        Logger.error(e);
        mHandler.uncaughtException(t, e);
    }

}
