/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.mda.telemetry;

import android.content.Context;
import android.text.TextUtils;

import com.carota.mda.data.UpdateCampaign;
import com.carota.mda.remote.IActionAPI;
import com.carota.mda.remote.IActionSDA;
import com.carota.mda.remote.IActionVSI;
import com.carota.sync.DataSyncManager;
import com.carota.sync.analytics.UpgradeAnalyticsV2;
import com.carota.sync.analytics.VehicleAnalytics;
import com.carota.sync.uploader.AppLogUploader;
import com.carota.util.DatabaseHolderEx;
import com.momock.util.Logger;

import java.io.File;
import java.util.HashMap;

public class FotaAnalytics {

    public static class OTA {
        public static final int STATE_RECEIVED = 1;
        public static final int STATE_CANCEL = 2;
        public static final int STATE_DOWNLOADING = 3;
        public static final int STATE_DOWNLOADED = 4;
        public static final int STATE_UPGRADE = 5;
        public static final int STATE_SUCCESS = 6;
        public static final int STATE_FAIL = 7;
        public static final int STATE_DOWNLOAD_FAIL = 8;
        public static final int STATE_MD5_SUCCESS = 9;
        public static final int STATE_MD5_FAIL = 10;
        public static final int STATE_PKI_SUCCESS = 11;
        public static final int STATE_PKI_FAIL = 12;
        public static final int STATE_ROLLBACK_SUCCESS = 13;
        public static final int STATE_ROLLBACK_FAIL = 14;

        public static final int TARGET_UPGRADE_DST = -111;
        public static final int TARGET_UPGRADE_SRC = -112;
        public static final int TARGET_UPGRADE_CFG = -113;
    }

    private UpgradeAnalyticsV2 mUpgradeAnalyticsV2;
    private VehicleAnalytics mVehicleAnalytics;
    private AppLogUploader mAppLogUploader;
    private AppLogCollector mAppLogCollector;
    private AppLogFileChunk mAppLogFileChunk;

    private int mLogType;
    private HashMap<String, String> mLogPath;
    private String mToken;

    public FotaAnalytics(Context context,
                         IActionAPI actionAPI, IActionSDA actionSDA,
                         IActionVSI actionVSI) {

        DataSyncManager dsm = DataSyncManager.get(context);
        mUpgradeAnalyticsV2 = dsm.getSync(UpgradeAnalyticsV2.class);
        mVehicleAnalytics = dsm.getSync(VehicleAnalytics.class);
        mAppLogUploader = dsm.getSync(AppLogUploader.class);

        File logCacheDir = new File(context.getFilesDir(), "ColCacheALC");
        mAppLogFileChunk = new AppLogFileChunk(DatabaseHolderEx.getAppLog(context));
        mAppLogCollector = new AppLogCollector(context, logCacheDir, mAppLogUploader, mAppLogFileChunk);
    }

    public void setVinCode(String vin) {
        mVehicleAnalytics.setVIN(vin);
    }

    public boolean logUpgradeStateV2(String usid, String ecu, int state, int ecustate, int code, String erMsg) {
        return mUpgradeAnalyticsV2.logAction(usid, state, ecustate, ecu, code, erMsg);
    }

    public void setUlid(String ulid) {
        mAppLogCollector.active(ulid);
    }

    public void syncData() {
        mUpgradeAnalyticsV2.syncData();
        mVehicleAnalytics.syncData();
        mAppLogUploader.syncData();

    }

    public void setLogInfo(int logType, HashMap<String, String> logPath, String ulid, String usid) {
        mLogType = logType;
        mLogPath = logPath;
        mAppLogCollector.setPaths(mLogPath);
        mToken = TextUtils.isEmpty(ulid) ? usid : ulid;
        Logger.info("FotaAnalytics setLogInfo mLogType = " + mLogType + " / mToken = " + mToken + " / ulid = " + ulid + " / usid = " + usid);
        if (!TextUtils.isEmpty(ulid) && !hasUnfinishedWork()) {
            startCollectLog(AppLogFileChunk.TYPE_CLIENT);
        }
    }

    public void uploadLog(boolean isFail) {
        Logger.info("FotaAnalytics uploadLog");
        if (mLogType == UpdateCampaign.UPLOAD_LOG_WHEN_UPDATE_ERROR) {
            if (isFail) startCollectLog(AppLogFileChunk.TYPE_UPDATE);
        } else if (mLogType == UpdateCampaign.UPLOAD_LOG_WHEN_UPDATE_FINISH) {
            startCollectLog(AppLogFileChunk.TYPE_UPDATE);
        }
    }

    public boolean hasUnfinishedWork() {
        int state = mAppLogFileChunk.queryState();
        return state == 0 || state == 1 || state == 2 && !TextUtils.isEmpty(mAppLogUploader.getRequestId());
    }

    private void startCollectLog(int logType) {
        Logger.error("FotaAnalytics startCollectLog logType = " + logType + " / mToken = " + mToken);
        mAppLogFileChunk.cleanState();
        mAppLogFileChunk.updateType(logType);
        mAppLogCollector.active(mToken);
    }
}
