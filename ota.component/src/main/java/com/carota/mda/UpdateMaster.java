/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.mda;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;

import com.carota.build.IConfiguration;
import com.carota.build.ParamDM;
import com.carota.build.ParamHub;
import com.carota.build.ParamMDA;
import com.carota.build.ParamRoute;
import com.carota.core.ITask;
import com.carota.core.remote.ActionSH;
import com.carota.core.remote.IActionSH;
import com.carota.core.remote.info.HubInfo;
import com.carota.dtc.VehicleDTC;
import com.carota.mda.auto.AutoDeploy;
import com.carota.mda.data.BomDataCache;
import com.carota.mda.data.MasterDataCache;
import com.carota.mda.data.MasterStatus;
import com.carota.mda.data.UpdateCampaign;
import com.carota.mda.data.UpdateItem;
import com.carota.mda.deploy.IDeploySafety;
import com.carota.mda.deploy.TriggeredRecord;
import com.carota.mda.deploy.bean.DeployResult;
import com.carota.mda.deploy.ctrl.DeployCtrl;
import com.carota.mda.deploy.task.DeployTaskFactory;
import com.carota.mda.download.DownloadCtrl;
import com.carota.mda.download.DownloadEventHandler;
import com.carota.mda.download.IDownloadCtrlStatus;
import com.carota.mda.remote.ActionAPI;
import com.carota.mda.remote.ActionSDA;
import com.carota.mda.remote.IActionAPI;
import com.carota.mda.remote.IActionSDA;
import com.carota.mda.remote.info.BomInfo;
import com.carota.mda.remote.info.EcuInfo;
import com.carota.mda.remote.info.VehicleDesc;
import com.carota.mda.security.ISecuritySolution;
import com.carota.mda.security.SecurityCenter;
import com.carota.mda.telemetry.FotaAnalytics;
import com.carota.mda.telemetry.FotaState;
import com.carota.sync.DataSyncManager;
import com.carota.sync.analytics.AppAnalytics;
import com.carota.util.ConfigHelper;
import com.carota.vsi.IVehicleDescription;
import com.carota.vsi.VehicleServiceManager;
import com.momock.util.JsonHelper;
import com.momock.util.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class UpdateMaster {

    private final AutoDeploy mAutoDeploy;
    private final DeployCtrl mCtrl;
    private Context mContext;
    private IActionAPI mActionAPI;
    private IActionSDA mActionSDA;
    private VehicleServiceManager mVehicleServiceManager;
    private IActionSH mActionSH;

    private ParamRoute mParamRoute;
    private ParamMDA mParamMDA;
    private ParamDM mParamDM;

    private UpdateCampaign mSession;
    private BomDataCache mBomDataCache;
    private MasterDataCache mMasterDataCache;
    private FotaAnalytics mAnalyze;
    private DownloadCtrl mDownloader;
    private MasterStatus mStatus;
    private SecurityCenter mSecurityCenter;

    public static final Object CALL_LOCKER = new Object();
    public static final Object SYNC_LOCKER = new Object();

    private static final int SYNC_DATA_TIME = 10 * 60 * 1000;

    private final Handler mHandler = new Handler(Looper.getMainLooper(),new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            DataSyncManager.get(mContext).syncData();
            mHandler.sendEmptyMessageDelayed(0, SYNC_DATA_TIME);
            return true;
        }
    });

    public UpdateMaster(Context context, ISecuritySolution solution,
                        IDeploySafety safety, IVehicleDescription description) {
        mContext = context.getApplicationContext();
        mStatus = new MasterStatus(mContext);
        IConfiguration cfg = ConfigHelper.get(mContext);
        mParamRoute = cfg.get(ParamRoute.class);
        mParamMDA = cfg.get(ParamMDA.class);
        mParamDM = cfg.get(ParamDM.class);
        mActionAPI = new ActionAPI();
        mActionSDA = new ActionSDA();
        mVehicleServiceManager = new VehicleServiceManager(mContext, description);
        mActionSH = new ActionSH(cfg.get(ParamHub.class).getHost());
        mBomDataCache = new BomDataCache(context);
        mMasterDataCache = new MasterDataCache(context);
        mAnalyze = new FotaAnalytics(mContext, mActionAPI, mActionSDA, mVehicleServiceManager);
        mSecurityCenter = new SecurityCenter(mContext, 60 * 1000, solution);
        mDownloader = new DownloadCtrl(mSecurityCenter, mParamMDA, mParamRoute);
        mAutoDeploy = new AutoDeploy(mContext, mAnalyze, mSecurityCenter);
        mCtrl = new DeployCtrl(new DeployTaskFactory.Builder(mContext)
                .setActionSDA(mActionSDA)
                .setParamRoute(mParamRoute)
                .setParamMDA(mParamMDA)
                .setAnalytics(mAnalyze)
                .setDownloader(mDownloader)
                .setSecure(mSecurityCenter)
                .setSafety(safety)
                .build());
    }

    public UpdateMaster init() {
        mHandler.sendEmptyMessageDelayed(0, SYNC_DATA_TIME >> 1);
        if (!mAutoDeploy.resume()) {
            resumeSession();
            resumeSDA();
        }
        return this;
    }

    private void resumeSDA() {
        if (mCtrl.getDbIsRun()) {
            triggerMasterUpgrade(mSession.getUSID());
        }
    }

    private void resumeSession() {
        UpdateCampaign us = new UpdateCampaign(mMasterDataCache.getConnData(), mBomDataCache.getBomInfoList());
        if(us.check() && mStatus.getPackage()) {
            mSession = us;
            mAnalyze.setLogInfo(mSession.getLogType(), mSession.getLogPath(), mSession.getULID(), mSession.getUSID());
        }
    }

    public List<EcuInfo> queryEcuInfo() {
        if(isInstalling()) {
            return mMasterDataCache.getEcuInfoList();
        } else {
            return refreshEcuInfoList();
        }
    }

    private List<EcuInfo> refreshEcuInfoList() {
        synchronized (UpdateMaster.SYNC_LOCKER) {
            Logger.debug("Sync ECU INFO");
            List<ParamRoute.Info> ecus = mParamRoute.listEcuInfo();
            LinkedHashMap<String, EcuInfo> map = new LinkedHashMap<>();
            for (ParamRoute.Info info : ecus) {
                EcuInfo ecuInfo = mActionSDA.queryInfo(ParamRoute.getEcuHost(info), info.ID, null);
                if (ecuInfo != null) {
                    map.put(info.ID, ecuInfo);
                }
            }
            List<BomInfo> bomInfos = mBomDataCache.getBomInfoList();
            List<BomInfo> validBomInfoList = new ArrayList<>();
            for (BomInfo info : bomInfos) {
                EcuInfo ecuInfo = mActionSDA.queryInfo(mParamRoute.getSubHost(), info.getName(), info);
                String flashHv = info.getFlashConfig().getHv();
                // TODO
                if (ecuInfo != null && !flashHv.isEmpty() && flashHv.equals(ecuInfo.hwVer)) {
                    map.put(info.getName(), ecuInfo);
                    validBomInfoList.add(info);
                }
            }
            mBomDataCache.setBomInfo(validBomInfoList);
            List<EcuInfo> infoList = new ArrayList<>(map.values());
            mMasterDataCache.setEcuInfo(infoList);
            return infoList;
        }
    }

    // Must CALL after method reset();
    public UpdateCampaign doConnectToServer(String lang, boolean isFactory) {
        if(mAutoDeploy.isRunning()) {
            Logger.info("Auto is Running");
            return null;
        }
        if (isInstalling()) {
            Logger.info("Deplay is Running");
            return null;
        }
        if (mAnalyze.hasUnfinishedWork()) {
            Logger.info("Has unfinished upload app log work");
            return null;
        }
        long start = SystemClock.elapsedRealtime();
        VehicleDesc vd = mVehicleServiceManager.queryInfo();

        syncBoms(mParamMDA.getBomUrl(), vd.getVin());

        List<EcuInfo> infos = refreshEcuInfoList();
        long waitTime = 5000 - (SystemClock.elapsedRealtime() - start);
        if (waitTime > 0) {
            //wait auto end
            try {
                Thread.sleep(waitTime);
            } catch (InterruptedException e) {
                Logger.error(e);
            }
        }
        synchronized (UpdateMaster.CALL_LOCKER) {
            if (!TextUtils.isEmpty(vd.getVin())) {
                VehicleDTC.get().activeDtcTask(mContext,vd.getVin(),infos);
                String data = mActionAPI.connect(mParamMDA.getConnUrl(), vd.getVin(), lang, infos, isFactory);
                JSONObject joResp = JsonHelper.parseObject(data);
                if (null != joResp && joResp.has(UpdateCampaign.PROP_VIN)) {
                    mSession = new UpdateCampaign(joResp, mBomDataCache.getBomInfoList());
                    // set upload log task
                    //mAnalyze.setUlid(mSession.getULID());
                    mAnalyze.setLogInfo(mSession.getLogType(), mSession.getLogPath(), mSession.getULID(), mSession.getUSID());
                    // report event
                    if (mSession.check()) {
                        String usid = mSession.getUSID();
                        mStatus.setUSID(usid);
                        for (int i = 0; i < mSession.getItemCount(); i++) {
                            UpdateItem task = mSession.getItem(i);
                            String name = task.getProp(UpdateItem.PROP_NAME);
                            boolean hasSecurity = task.getProp(UpdateItem.PROP_HAS_SECURITY, Boolean.FALSE);
                            if (hasSecurity) {
                                mSecurityCenter.checkSecurityInfo(task, mSession.getTokenUrl(), usid);
                            }
                            if(i == mSession.getItemCount() -1) {
                                mAnalyze.logUpgradeStateV2(usid, name, FotaState.OTA.STATE_RECEIVED, FotaState.OTA.STATE_RECEIVED, 0, "");
                            }else{
                                mAnalyze.logUpgradeStateV2(usid, name, FotaState.OTA.STATE_CONNECT, FotaState.OTA.STATE_RECEIVED, 0, "");
                            }
                        }
                    } else {
                        mDownloader.stop(true);
                    }
                    // process by auto-install or not
                    if(!mAutoDeploy.process(mSession)) {
                        mMasterDataCache.setConnData(joResp);
                        activeSyncAnalyticsData();
                        return mSession;
                    }
                }
                mMasterDataCache.setConnData(null);
                if(null != mSession) {
                    //WARNING: MUST create empty response for auto-install
                    // or UI will received error on connect callback
                    JSONObject empty = JsonHelper.parseObject(data);
                    empty.remove("ecus");
                    return new UpdateCampaign(empty, mBomDataCache.getBomInfoList());
                }
            }
            activeSyncAnalyticsData();
            return null;
        }
    }

    private String syncBoms(String url, String vin) {
        String bomData = mActionAPI.syncBom(url, vin);
        Logger.debug("@UM bomData : " + bomData);
        JSONObject jObj = JsonHelper.parseObject(bomData);
        if (jObj != null) {
            JSONArray jsonArray = jObj.optJSONArray("data");
            List<BomInfo> bomInfoList = new ArrayList<>();
            try {
                if (jsonArray != null && jsonArray.length() > -1) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);
                        bomInfoList.add(BomInfo.fromJson(obj));
                    }
                }
            } catch (Exception e) {
                Logger.error(e);
            }
            mBomDataCache.setBomInfo(bomInfoList);
        }
        return bomData;
    }

    public boolean startDownload() {
        synchronized (UpdateMaster.CALL_LOCKER) {
            if (null != mSession) {
                mDownloader.reset(mSession, new DownloadEventHandler(mAnalyze, mStatus));
                mDownloader.start();
                return mDownloader.isRunning();
            }
            return false;
        }
    }

    public void stopDownload() {
        mDownloader.stop(false);
    }

    public IDownloadCtrlStatus getDownloadController() {
        return mDownloader;
    }

    public UpdateCampaign getCampaign() {
        return mSession;
    }

    public MasterStatus getStatus() {
        return mStatus;
    }

    public UpdateCampaign SynchronousData(boolean verify, AtomicBoolean serverIsAvailable) {
        if (null != mSession && mSession.check()) {
            if(!verify) {
                return mSession;
            }
            String vmid = mSession.getProp(UpdateCampaign.PROP_VMID);
            String scheduleId = mSession.getScheduleId();
            Map<String, String> extra = new HashMap<>();
            extra.put("usid", mSession.getUSID());
            extra.put("vin", mSession.getVinCode());

            AtomicBoolean verifyResult = new AtomicBoolean();

            boolean reqRet = mActionAPI.confirmExpired(
                    mParamMDA.getCheckUrl(), vmid, scheduleId, extra, verifyResult);
            Logger.error("Session Verify : " + reqRet);
            serverIsAvailable.set(reqRet);
            if(!reqRet || verifyResult.get()) {
                return mSession;
            }
            String ecuName = "tbox";
            if(mSession.getItemCount() > 0) {
                ecuName = mSession.getItem(0).getProp(ITask.PROP_NAME);
            }
            mAnalyze.logUpgradeStateV2(mSession.getUSID(), ecuName, FotaState.OTA.STATE_UPDATE_INTERRUPT, FotaState.OTA.STATE_UPDATE_INTERRUPT, FotaState.OTA.INTERRUPT.CODE_INTERRUPT_INVALID, "");
            Logger.error("Session expired");
        }
        return null;
    }

    private void activeSyncAnalyticsData() {
        mAnalyze.syncData();
    }

    public boolean triggerSlaveUpgrade(String usid) {
        synchronized (UpdateMaster.CALL_LOCKER) {
            if (mStatus.getUSID().isEmpty()) {
                Logger.info("Missing usid");
                return false;
            }
            if (!isInstalling()) {
                Logger.error("Clear Result");
                TriggeredRecord.get(mContext).reset();
                mStatus.setUpgrade(false, true);
            }
            return true;
        }
    }

    public int triggerMasterUpgrade(String usid) {
        synchronized (UpdateMaster.CALL_LOCKER) {
            if (isInstalling()) {
                if (mStatus.getUpgradeMaster()) {
                    Logger.info("upgrade Master is running");
                    return 1;
                } else {
                    Logger.info("upgrade Slave is running");
                    return 0;
                }
            }
            if (mStatus.getUSID().isEmpty()) {
                Logger.info("Missing usid");
                return -1;
            }
            mStatus.setUpgrade(true, true);
            mCtrl.start(mSession);
            return 1;
        }
    }

    public List<String> testSystemRpc() {
        List<String> ret = new ArrayList<>();
        HubInfo hubInfo = mActionSH.queryInfo();
        if (null != hubInfo) {
            for (ParamRoute.Info pri : mParamRoute.listInfo(ParamRoute.Info.PATH_ETH)) {
                if (!hubInfo.contains(ParamRoute.getEcuHost(pri))) {
                    ret.add(pri.ID);
                }
            }
            return ret;
        }
        // error in check service hub connection
        Logger.error("UM : Fail to test RPC");
        return null;
    }

    public boolean isDownloading() {
        return mDownloader.isRunning();
    }

    public boolean isInstalling() {
        return mCtrl.isRun();
    }

    public boolean reset() {
        synchronized (UpdateMaster.CALL_LOCKER) {
            if (!isInstalling()) {
                stopDownload();
                mStatus.reset();
                mSession = null;
                return true;
            }
            return false;
        }
    }

    public VehicleServiceManager getVehicleServiceManager() {
        return mVehicleServiceManager;
    }

    public DeployResult getResult() {
        DeployResult result = mCtrl.getResult();
        return result == null ? new DeployResult(mSession.getUSID()) : result;
    }

    public boolean sendUIPoint(long at, int id, String msg) {
        return DataSyncManager.get(mContext).getSync(AppAnalytics.class)
                .logAction(mSession.getUSID(), id, msg, at);

    }

    public boolean sendUIEvent(long at, int upgradeType, int eventCode, String msg,
                               int result, String scheduleId, int eicSystem) {
        return DataSyncManager.get(mContext).getSync(AppAnalytics.class)
                .logAction(mSession.getUSID(), at, upgradeType,eventCode,msg,result,scheduleId,eicSystem);
    }

    public boolean sendFotaData(int totalState, String ecu, int state, int code, long time, String error) {
        return mAnalyze.logUpgradeStateV2(mSession.getUSID(), ecu, totalState, state, code, error);
    }
}
