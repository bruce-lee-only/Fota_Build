/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/
package com.carota.sota.store;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.SystemClock;
import android.text.TextUtils;

import com.carota.build.ParamSOTA;
import com.carota.dm.file.app.AppFileManager;
import com.carota.dm.file.IFileManager;
import com.carota.dm.down.FileDownloader;
import com.carota.dm.down.IDownCallback;
import com.carota.dm.down.IFileDownloader;
import com.carota.sota.IApplyUpgradeCallback;
import com.carota.sota.ICheckResultCallback;
import com.carota.sota.db.SoftwareDb;
import com.carota.sota.remote.ActionSOTA;
import com.carota.sota.remote.IActionSOTA;
import com.carota.sota.util.AppInstallHelper;
import com.carota.sota.util.RequestState;
import com.carota.sync.DataSyncManager;
import com.carota.sync.analytics.SoftwareAnalytics;
import com.carota.util.ConfigHelper;
import com.carota.util.DatabaseHolderEx;
import com.momock.util.EncryptHelper;
import com.momock.util.FileHelper;
import com.momock.util.JsonDatabase;
import com.momock.util.Logger;

import org.json.JSONObject;

import java.io.File;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicBoolean;

public class AppManager {
    //status code
    private static final int RECEIVED_NEW_VERSION = 1;
    private static final int DOWNLOADING = 2;
    private static final int DOWNLOAD_COMPLETE = 3;
    private static final int MD5_OK = 4;
    private static final int START_INSTALL = 5;
    private static final int INSTALL_COMPLETE = 6;
    private static final int INSTALL_ERROR = 7;
    private static final int DOWNLOAD_ERROR = 8;
    private static final int MD5_ERROR = 9;
    //error code
    private static final int DOWNLAO_IDEL_ERROR_CODE = 1;
    private static final int DOWNLAO_ERROR_CODE = 2;
    private static final int CREATE_SESSION_ERROR_CODE = 3;
    private static final int COPY_ERROR_CODE = 4;
    private static final int EXEC_INSTALL_ERROR_CODE = 5;

    private IActionSOTA mActionSOTA;
    private Context mContext;
    private PackageManager mPackageManager;
    private String mSavePath;
    private AppData mAppData;
    private IFileDownloader mDownloader = null;
    private int maxRetry;
    private Comparator<AppInfo> mComparator;
    private int mCurrentIndex = -1;
    private ParamSOTA mParamSOTA;
    private String mAppsUrl;
    private String mDataUrl;
    private String mVin;
    private JsonDatabase.Collection mCollection;
    private SoftwareDb mSoftwareDb;
    private String mPkgName;
    private final AtomicBoolean mSyncReportAppInfoData;
    private final AtomicBoolean hasSyncReportAppInfoDataComplete;
    private final AtomicBoolean mCheckSelfUpgrade;
    private final AtomicBoolean hasCheckSelfUpgradeComplete;
    private SoftwareAnalytics mSoftwareAnalytics;

    public AppManager(Context context) {
        mContext = context;
        mCollection = DatabaseHolderEx.getSotaData(mContext);
        mSoftwareDb = new SoftwareDb(mCollection);
        mParamSOTA = ConfigHelper.get(mContext).get(ParamSOTA.class);
        maxRetry = mParamSOTA.getMaxRetry();
        mAppsUrl = mParamSOTA.getAppsUrl();
        mDataUrl = mParamSOTA.getDataUrl();
        mActionSOTA = new ActionSOTA();
        mPackageManager = mContext.getPackageManager();
        mSavePath = mContext.getExternalCacheDir().getAbsolutePath();
        mPkgName = mContext.getPackageName();
        mSyncReportAppInfoData = new AtomicBoolean(false);
        hasSyncReportAppInfoDataComplete = new AtomicBoolean(false);
        mCheckSelfUpgrade = new AtomicBoolean(false);
        hasCheckSelfUpgradeComplete = new AtomicBoolean(false);
        mSoftwareAnalytics = DataSyncManager.get(mContext).getSync(SoftwareAnalytics.class);
        mSoftwareAnalytics.syncData();
        syncReportAppInfoData();
        checkSelfUpgrade();
    }

    private boolean doAppDownload(UpdateItem item, IApplyUpgradeCallback callback) {
        recordAppInstallInfo(item, DOWNLOADING);
        String url = mAppData.getFileUrl(item.getMd5());
        File appFile = getAppFile(item);
        IFileManager fileManager = new AppFileManager(appFile.getParentFile(), "APP");
        String tmp = appFile.getName().concat(".tmp");
        mDownloader = new FileDownloader(url,tmp , fileManager, maxRetry, 0, new IDownCallback() {
            @Override
            public void progress(int speed, long length, long fileLength) {
                callback.onDownloading(mAppData, mCurrentIndex, (int) (length * 100 / fileLength));
            }
        });
        int errorCode = 0;
        try {
            switch (mDownloader.start()) {
                case IFileDownloader.CODE_SUCCESS:
                    Logger.debug("DT-SOTA-EXEC : downloaded");
                    fileManager.renameFile(tmp, appFile.getName());
                    recordAppInstallInfo(item, DOWNLOAD_COMPLETE);
                    return true;
                case IFileDownloader.CODE_CANCLE:
                    Logger.error("DT-SOTA-EXEC : STOP ");
                    errorCode = DOWNLAO_IDEL_ERROR_CODE;
                    break;
                default:
                    Logger.error("DT-SOTA-EXEC : ERROR");
                    errorCode = DOWNLAO_ERROR_CODE;
                    break;
            }
        } catch (Exception e) {
            Logger.error("DT-SOTA-EXEC : EXCP @ ");
            Logger.error(e);
        }
        recordAppInstallInfo(item, DOWNLOAD_ERROR, errorCode);
        return false;
    }

    private boolean doAppMd5Check(UpdateItem item, String filePath) {
        File file = new File(filePath);
        if (file == null || !file.isFile()) {
            Logger.error("doAppMd5Check file error " + filePath);
            return false;
        }
        String md5 = EncryptHelper.calcFileMd5(file);
        Logger.debug("doAppMd5Check md5 = " + md5 + " / appmd5 = " + item.getMd5());
        if (md5.equals(item.getMd5())) {
            recordAppInstallInfo(item, MD5_OK);
            return true;
        }
        recordAppInstallInfo(item, MD5_ERROR);
        return false;
    }

    private void doAppInstall(UpdateItem item, IApplyUpgradeCallback callback) {
        Logger.debug("doAppInstall item = " + item.getPackageName() + " / mPkgName = " + mPkgName);
        if (item.getPackageName().equals(mPkgName)) {
            mSoftwareDb.saveSelfInfo(mVin, item.getPackageName(), item.getVersionCode(), item.getVersionName(), item.getSchedule(), item.getId());
        }
        recordAppInstallInfo(item, START_INSTALL);
        callback.onInstalling(mAppData, mCurrentIndex, 0);
        int result = AppInstallHelper.install(mContext, getAppPath(item), mPackageManager);
        if (result == AppInstallHelper.INSTALL_COMPLETE) {
            recordAppInstallInfo(item, INSTALL_COMPLETE);
            callback.onFinished(mAppData, mCurrentIndex);
            mSoftwareDb.deleteCampaignState(item.getPackageName());
        } else {
            int errorCode = 0;
            switch (result) {
                case AppInstallHelper.CREATE_SESSION_ERROR:
                    errorCode = CREATE_SESSION_ERROR_CODE;
                    break;
                case AppInstallHelper.COPY_ERROR:
                    errorCode = COPY_ERROR_CODE;
                    break;
                case AppInstallHelper.EXEC_INSTALL_ERROR:
                    errorCode = EXEC_INSTALL_ERROR_CODE;
                    break;
            }
            recordAppInstallInfo(item, INSTALL_ERROR, errorCode);
            callback.onError(mAppData, mCurrentIndex, result);
            int insRetryCount = mSoftwareDb.findInsRetryCountByName(item.getPackageName());
            mSoftwareDb.setCampaignState(item.getPackageName(), ++insRetryCount, item.getId());
        }
    }

    private String getAppPath(UpdateItem item) {
        return mSavePath + "/" + item.getPackageName() + ".apk";
    }

    private File getAppFile(UpdateItem item) {
        return new File(mSavePath, item.getPackageName() + ".apk");
    }

    public void setAppInfoComparator(Comparator<AppInfo> appInfoComparator) {
        mComparator = appInfoComparator;
    }

    private void recordAppInstallInfo(UpdateItem item, int status) {
        recordAppInstallInfo(item, status, 0);
    }

    private void recordAppInstallInfo(UpdateItem item, int status, int errorCode) {
        Logger.info("recordAppInstallInfo pkg = %s / status = %d", item.getPackageName(), status);
        mSoftwareAnalytics.logState(mVin, item.getId(), item.getSchedule(), status, errorCode);
        if (status == INSTALL_COMPLETE) {
            FileHelper.delete(new File(mContext.getExternalCacheDir(), item.getPackageName() + ".apk"));
        }
    }

    public void checkUpgrade(String vin, String brand, String model, ICheckResultCallback callback) {
        if (!mParamSOTA.isEnabled()) {
            Logger.debug(" SOTA is not Enabled ");
            callback.onError(ICheckResultCallback.NOT_SUPPORT, "Not Support");
            return;
        }

        if (!waitSyncReportAppInfoDataReady() || waitSyncReportAppInfoDataReady() && !mSyncReportAppInfoData.get()) {
            callback.onError(ICheckResultCallback.CONNECT_ERROR, "connect error");
            return;
        }

        if (!waitCheckSelfUpgradeReady() || waitCheckSelfUpgradeReady() && !mCheckSelfUpgrade.get()) {
            callback.onError(ICheckResultCallback.CONNECT_ERROR, "connect error");
            return;
        }
        mVin = vin;
        UpdateCampaign campaign = mActionSOTA.queryAppList(mAppsUrl, vin, brand, model, null);

        if (null == campaign) {
            Logger.debug(" checkUpgrade connect error ");
            callback.onError(ICheckResultCallback.CONNECT_ERROR, "connect error");
            return;
        }
        AppData appData = new AppData(campaign);
        RequestState state = campaign.getState();
        Logger.debug("checkUpgrade RequestState = %s", state.toString());
        callback.onConnected(state.code);
        if (IActionSOTA.RESPONSE_OK == state.code) {
            Logger.debug("checkUpgrade itemcount = " + campaign.getItemCount());
            if (campaign.getItemCount() == 0) {
                callback.onResult(null);
            } else {
                UpdateItem selfUpdateItem = null;
                for (int i = 0; i < campaign.getItemCount(); i++) {
                    UpdateItem item = campaign.getItem(i);
                    String packageName = item.getPackageName();
                    String lastId = mSoftwareDb.findIdByName(packageName);
                    int insRetryCount = mSoftwareDb.findInsRetryCountByName(packageName);
                    if (!TextUtils.isEmpty(lastId)) {
                        if (!lastId.equals(item.getId())) {
                            mSoftwareDb.deleteCampaignState(packageName);
                        } else if (insRetryCount >= 3) {
                            continue;
                        }
                    }
                    PackageInfo packageInfo = null;
                    try {
                        packageInfo = mPackageManager.getPackageInfo(item.getPackageName(), 0);
                    } catch (PackageManager.NameNotFoundException e) {
                    }
                    if (packageInfo != null) {
                        AppInfo oldAppInfo = new AppInfo(packageInfo.packageName, packageInfo.versionCode, packageInfo.versionName);
                        int vcNew = item.getVersionCode();
                        int vcOld = oldAppInfo.getVersionCode();
                        if ((vcOld > 0 && vcOld < vcNew) || (vcOld == vcNew && mComparator != null && mComparator.compare(oldAppInfo, item) < 0)) {
                            if (item.getPackageName().equals(mPkgName)) {
                                selfUpdateItem = item;
                            } else {
                                recordAppInstallInfo(item, RECEIVED_NEW_VERSION);
                                appData.setValidItem(item);
                            }
                        }
                    }
                }
                if (selfUpdateItem != null) {
                    recordAppInstallInfo(selfUpdateItem, RECEIVED_NEW_VERSION);
                    appData.setValidItem(selfUpdateItem);
                }
                if (appData.getAppInfoCount() == 0) {
                    callback.onResult(null);
                } else {
                    callback.onResult(appData);
                }
            }
        } else {
            Logger.error(" checkUpgrade server error %s", appData.getMsg());
            callback.onError(mAppData.getCode(), appData.getMsg());
        }

    }

    public void applyUpgrade(AppData appData, IApplyUpgradeCallback callback) {
        mAppData = appData;
        for (int i = 0; i < appData.getAppInfoCount(); i++) {
            mCurrentIndex = i;
            UpdateItem item = (UpdateItem) appData.getAppInfo(i);
            Logger.debug("applyUpgrade index = " + i + " / item = " + item.getPackageName());
            if (doAppDownload(item, callback)) {
                if (doAppMd5Check(item, getAppPath(item))) {
                    doAppInstall(item, callback);
                } else {
                    Logger.error("applyUpgrade app md5 error %s", item.getPackageName());
                    callback.onError(mAppData, mCurrentIndex, AppInstallHelper.MD5_ERROR);
                }
            } else {
                Logger.error("applyUpgrade app download error %s", item.getPackageName());
                callback.onError(mAppData, mCurrentIndex, AppInstallHelper.DOWNLOAD_ERROR);
            }
        }
    }

    private void checkSelfUpgrade() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (!waitSyncReportAppInfoDataReady() || waitSyncReportAppInfoDataReady() && !mSyncReportAppInfoData.get()) {
                    hasCheckSelfUpgradeComplete.set(true);
                    return;
                }
                JSONObject selfObJest = mSoftwareDb.findSelfInfo();
                Logger.debug("checkSelfUpgrade selfObJest = " + selfObJest);
                if (selfObJest != null) {
                    String vin = selfObJest.optString("vin");
                    String vn = selfObJest.optString("versionName");
                    int vc = selfObJest.optInt("versionCode", -1);
                    int schedule = selfObJest.optInt("schedule");
                    String id = selfObJest.optString("id");
                    PackageInfo packageInfo = null;
                    int status = INSTALL_ERROR;
                    try {
                        packageInfo = mPackageManager.getPackageInfo(mPkgName, 0);
                    } catch (PackageManager.NameNotFoundException e) {
                    }
                    if (packageInfo != null) {
                        Logger.debug("checkSelfUpgrade packageInfo vn = %s / vc = %d", packageInfo.versionName, packageInfo.versionCode);
                        if (packageInfo.versionName.equals(vn) && packageInfo.versionCode == vc) {
                            status = INSTALL_COMPLETE;
                            FileHelper.delete(new File(mContext.getExternalCacheDir(), mPkgName + ".apk"));
                            mSoftwareDb.deleteCampaignState(mPkgName);
                        } else {
                            int insRetryCount = mSoftwareDb.findInsRetryCountByName(mPkgName);
                            mSoftwareDb.setCampaignState(mPkgName, ++insRetryCount, id);
                        }
                        mSoftwareAnalytics.logState(vin, id, schedule, status, EXEC_INSTALL_ERROR_CODE);
                        long endTime = SystemClock.elapsedRealtime() + 5 * 60 * 1000;
                        while (mSoftwareAnalytics.isSyncing()) {
                            if (SystemClock.elapsedRealtime() > endTime) {
                                Logger.debug("record self info timeout");
                                break;
                            }
                            try {
                                Thread.sleep(2000);
                            } catch (Exception e) {
                                throw new RuntimeException("Interrupted @ Wait record self info");
                            }
                        }

                        if (!mSoftwareAnalytics.isSyncing()) {
                            mCheckSelfUpgrade.set(true);
                        }
                        mSoftwareDb.deleteSelfInfo();
                    }
                } else {
                    mCheckSelfUpgrade.set(true);
                }
                hasCheckSelfUpgradeComplete.set(true);
            }
        }).start();
    }

    private void syncReportAppInfoData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                long endTime = SystemClock.elapsedRealtime() + 5 * 60 * 1000;
                if (!mSoftwareAnalytics.isSyncing()) {
                    mSyncReportAppInfoData.set(true);
                    hasSyncReportAppInfoDataComplete.set(true);
                } else {
                    try {
                        while (mSoftwareAnalytics.isSyncing()) {
                            if (SystemClock.elapsedRealtime() > endTime) {
                                Logger.debug("syncReportAppInfoData timeout");
                                hasSyncReportAppInfoDataComplete.set(true);
                                return;
                            }
                            try {
                                Thread.sleep(2000);
                            } catch (Exception e) {
                                throw new RuntimeException("Interrupted @ Wait record self info");
                            }
                            Logger.info("syncReportAppInfoData mSoftwareAnalytics.isSyncing() = " + mSoftwareAnalytics.isSyncing());
                        }
                    } catch (Exception e) {
                        Logger.error("syncReportAppInfoData error " + e.getMessage());
                    }
                }
                mSyncReportAppInfoData.set(true);
                hasSyncReportAppInfoDataComplete.set(true);
            }
        }).start();
    }

    private boolean waitSyncReportAppInfoDataReady() {
        Logger.debug("waitSyncReportAppInfoDataReady hasSyncReportAppInfoDataComplete = %b", hasSyncReportAppInfoDataComplete.get());
        long endTime = SystemClock.elapsedRealtime() + 5 * 60 * 1000;
        synchronized (hasSyncReportAppInfoDataComplete) {
            while (!hasSyncReportAppInfoDataComplete.get()) {
                if (SystemClock.elapsedRealtime() > endTime) {
                    Logger.debug("waitSyncReportAppInfoDataReady timeout");
                    return false;
                }
                try {
                    Thread.sleep(2000);
                } catch (Exception e) {
                    throw new RuntimeException("Interrupted @ Wait waitSyncReportAppInfoDataReady");
                }
                Logger.debug("waitSyncReportAppInfoDataReady hasSyncReportAppInfoDataComplete = %b", hasSyncReportAppInfoDataComplete.get());
            }
        }
        return true;
    }

    private boolean waitCheckSelfUpgradeReady() {
        Logger.debug("waitCheckSelfUpgradeReady mCheckSelfUpgrade = %b", hasCheckSelfUpgradeComplete.get());
        long endTime = SystemClock.elapsedRealtime() + 5 * 60 * 1000;
        synchronized (hasCheckSelfUpgradeComplete) {
            while (!hasCheckSelfUpgradeComplete.get()) {
                if (SystemClock.elapsedRealtime() > endTime) {
                    Logger.debug("waitCheckSelfUpgradeReady timeout");
                    return false;
                }
                try {
                    Thread.sleep(2000);
                } catch (Exception e) {
                    throw new RuntimeException("Interrupted @ Wait CheckSelfUpgrade");
                }
                Logger.debug("waitCheckSelfUpgradeReady mCheckSelfUpgrade = %b", hasCheckSelfUpgradeComplete.get());
            }
        }
        return true;
    }
}
