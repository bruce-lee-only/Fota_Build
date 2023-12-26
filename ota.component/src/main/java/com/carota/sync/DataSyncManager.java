/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.sync;

import android.content.Context;

import com.carota.build.IConfiguration;
import com.carota.build.ParamAnalytics;
import com.carota.build.ParamDTC;
import com.carota.build.ParamRoute;
import com.carota.build.ParamSOTA;
import com.carota.sync.analytics.AppAnalytics;
import com.carota.sync.analytics.SoftwareAnalytics;
import com.carota.sync.analytics.UpgradeAnalyticsV2;
import com.carota.sync.analytics.VehicleAnalytics;
import com.carota.sync.base.DataLogger;
import com.carota.sync.uploader.AppLogUploader;
import com.carota.sync.uploader.SysLogUploader;
import com.carota.util.ConfigHelper;
import com.carota.util.DatabaseHolderEx;
import com.momock.util.FileHelper;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class DataSyncManager {

    public static DataSyncManager sAnalMgr = null;

    public static DataSyncManager get(Context context) {
        synchronized (DataSyncManager.class) {
            if (null == sAnalMgr) {
                sAnalMgr = new DataSyncManager(context);
                sAnalMgr.initSyncPool();
            }
        }
        return sAnalMgr;
    }

    private final Context mContext;
    private final Map<Class<?>, DataLogger<?>> mAnalPool;

    private DataSyncManager(Context context) {
        mContext = context.getApplicationContext();
        mAnalPool = new HashMap<>();
    }

    public void syncData() {
        for (Map.Entry<Class<?>, DataLogger<?>> entry : mAnalPool.entrySet()) {
            entry.getValue().syncData();
        }
    }

    public <T extends DataLogger<?>> T getSync(Class<T> klass) {
        return klass.cast(mAnalPool.get(klass));
    }

    private <T extends DataLogger<?>> void addSync(T data) {
        mAnalPool.put(data.getClass(), data);
    }

    private void initSyncPool() {
        File fileCacheDir = new File(mContext.getFilesDir(), "SyncCache");
        if (!fileCacheDir.exists()) {
            FileHelper.mkdir(fileCacheDir);
        }

        IConfiguration cfg = ConfigHelper.get(mContext);
        ParamRoute paramRoute = cfg.get(ParamRoute.class);
        ParamAnalytics paramAnal = cfg.get(ParamAnalytics.class);
        ParamSOTA paramSota = cfg.get(ParamSOTA.class);
        ParamDTC paramDtc = cfg.get(ParamDTC.class);

        addSync(new UpgradeAnalyticsV2(DatabaseHolderEx.getFotaV2State(mContext), paramAnal));
        addSync(new AppAnalytics(DatabaseHolderEx.getAppEvent(mContext), paramAnal.getEventUrl()));
        addSync(new AppLogUploader(DatabaseHolderEx.getAppLog(mContext), fileCacheDir, paramAnal.getLogUrl()));

        addSync(new VehicleAnalytics(DatabaseHolderEx.getVehicleEvent(mContext), paramAnal.getCustomUrl(), paramRoute));

        addSync(new SoftwareAnalytics(DatabaseHolderEx.getSotaState(mContext), paramSota.getDataUrl()));

        addSync(new SysLogUploader(DatabaseHolderEx.getSysLog(mContext), fileCacheDir, paramDtc.getUploadUrl()));
    }
}
