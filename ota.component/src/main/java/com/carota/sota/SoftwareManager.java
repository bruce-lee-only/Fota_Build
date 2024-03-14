/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/
package com.carota.sota;

import android.content.Context;

import com.carota.sota.store.AppData;
import com.carota.sota.store.AppInfo;
import com.carota.sota.store.AppManager;
import com.carota.util.SerialExecutor;
import com.momock.util.Logger;

import java.util.Comparator;

public class SoftwareManager {

    private static final Object sLocker = new Object();
    private static AppManager sAppManager;
    private static final SerialExecutor sExecutor = new SerialExecutor();

    private static void initAppManager(Context context) {
        synchronized (sLocker) {
            if (sAppManager == null) {
                sAppManager = new AppManager(context.getApplicationContext());
            }
        }
    }

    public static void checkUpgrade(Context context, String vin, String brand, String model, ICheckResultCallback callback) {
        initAppManager(context);
        if (!sExecutor.isEmpty() || sExecutor.isRunning()) {
            Logger.debug("checkUpgrade is running return");
            return;
        }
        sExecutor.execute(new Runnable() {
            @Override
            public void run() {
                sAppManager.checkUpgrade(vin, brand, model, callback);
            }
        });
    }

    public static void applyUpgrade(Context context, AppData appData, IApplyUpgradeCallback callback) {
        initAppManager(context);
        if (!sExecutor.isEmpty() || sExecutor.isRunning()) {
            Logger.debug("applyUpgrade is alrea running");
            return;
        }
        sExecutor.execute(new Runnable() {
            @Override
            public void run() {
                sAppManager.applyUpgrade(appData, callback);
            }
        });
    }

    public static void setComparator(Context context, Comparator<AppInfo> comp) {
        initAppManager(context);
        sAppManager.setAppInfoComparator(comp);
    }
}
