/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.core.remote;

import android.content.BroadcastReceiver;
import android.os.Bundle;
import android.text.TextUtils;

import com.carota.core.ClientState;
import com.carota.core.ISession;
import com.carota.core.ITask;
import com.carota.core.VehicleEvent;
import com.carota.core.data.UpdateSession;
import com.carota.core.remote.info.DownloadProgress;
import com.carota.core.remote.info.InstallProgress;
import com.carota.core.remote.info.MDAInfo;
import com.carota.core.remote.info.VehicleInfo;
import com.carota.mda.remote.info.EcuInfo;
import com.carota.protobuf.MasterDownloadAgent;
import com.carota.protobuf.VehicleStatusInformation;
import com.carota.svr.PrivReqHelper;
import com.carota.svr.PrivStatusCode;
import com.carota.util.LogUtil;
import com.carota.util.ReqTag;
import com.google.protobuf.InvalidProtocolBufferException;
import com.momock.util.JsonHelper;
import com.momock.util.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class ActionMDA implements IActionMDA {

    private final String mBaseUrl;

    public ActionMDA(String host, int port) {
        if(port > 0) {
            mBaseUrl = "http://" + host + ":" + port;
        } else {
            mBaseUrl = "http://" + host;
        }
    }

    @Override
    public UpdateSession connect(String action, Bundle extra) {
        final String CALL_TAG = LogUtil.TAG_RPC_MDA + "[CONN] ";
        Logger.info(CALL_TAG + "%1s", action);
        try {
            MasterDownloadAgent.ConnReq.Builder builder = MasterDownloadAgent.ConnReq.newBuilder()
                    .setTag(ReqTag.TAG_SRC_CORE);
            switch (action) {
                case CONN_ACTION_CHECK:
                    if (extra != null && extra.getBoolean("isFactory",false)) {
                        builder.setAction(MasterDownloadAgent.ConnReq.Action.FACTORY);
                    } else {
                        builder.setAction(MasterDownloadAgent.ConnReq.Action.CHECK);
                    }
                    break;
                case CONN_ACTION_SYNC:
                    builder.setAction(MasterDownloadAgent.ConnReq.Action.SYNC);
                    break;
                case CONN_ACTION_VERIFY:
                    builder.setAction(MasterDownloadAgent.ConnReq.Action.VERIFY);
                    break;
                case CONN_ACTION_RESCUE:
                    builder.setAction(MasterDownloadAgent.ConnReq.Action.RESCUE);
                default:
                    return null;
            }
            if(extra != null && extra.getString("lang")!=null)
                builder.setLang(extra.getString("lang"));
            PrivReqHelper.Response resp = PrivReqHelper.doPost(mBaseUrl + "/connect", builder.build().toByteArray());
            int statusCode = resp.getStatusCode();
            Logger.info(CALL_TAG + "RSP : %1d", statusCode);
            if (PrivStatusCode.OK.equals(statusCode) || PrivStatusCode.SRV_ACT_REMOTE.equals(statusCode)) {
                MasterDownloadAgent.ConnRsp rsp = MasterDownloadAgent.ConnRsp.parseFrom(resp.getBody());
                JSONArray jaEcus = new JSONArray();
                for(MasterDownloadAgent.ConnRsp.UpgradeInfo info : rsp.getInfoList()) {
                    jaEcus.put(new JSONObject()
                            .put(ITask.PROP_NAME, info.getName())
                            .put(ITask.PROP_DST_VER, info.getDstVer())
                            .put(ITask.PROP_SRC_VER, info.getSrcVer())
                            .put(ITask.PROP_DST_SIZE, info.getSize())
                            .put(ITask.PROP_RELEASE_NOTE,info.getRn())
                            .put(ITask.PROP_UPDATE_TIME,info.getTime())
                    );
                }

                String mode = "";
                switch(rsp.getMode()){
                    case BY_USER:
                        mode = ISession.MODE_USER_CONFIRM;
                        break;
                    case AUTO_DOWNLOAD:
                        mode =ISession.MODE_AUTO_DOWNLOAD;
                        break;
                    case AUTO_INSTALL:
                        mode = ISession.MODE_AUTO_INSTALL;
                        break;
                    case BY_USER_LIMIT:
                        mode = ISession.MODE_USER_LIMIT;
                        break;
                    case AUTO_INSTALL_SCHEDULE:
                        mode = ISession.MODE_AUTO_INSTALL_SCHEDULE;
                        break;
                    case AUTO_UPDATE_FACTORY:
                        mode = ISession.MODE_AUTO_UPDATE_FACTORY;
                        break;
                    case BY_RESCUE:
                        mode = ISession.MODE_RESCUE;
                        break;
                }

                JSONObject joRoot = new JSONObject()
                        .put(UpdateSession.PROP_USID, rsp.getUsid())
                        .put("strategy_desc", new JSONObject().put("rn", rsp.getRn()))
                        .put(UpdateSession.PROP_CONDITION, new JSONArray(rsp.getEnvironmentList()))
                        .put(UpdateSession.PROP_VIN, rsp.getVin())
                        .put(UpdateSession.PROP_OPERATION, new JSONArray(rsp.getOperationList()))
                        .put(UpdateSession.PROP_MODE,mode)
                        .put(UpdateSession.PROP_CAMPAIGN_ID,rsp.getCampaignId())
                        .put(UpdateSession.PROP_APPOINTMENT_TIME,rsp.getAppointmentTime())
                        .put(UpdateSession.PROP_SCHEDULE_ID, rsp.getScheduleId())
                        .put(UpdateSession.PROP_UPDATE_TIME, rsp.getUpdateTime())
                        .put(UpdateSession.DISPLAY_INFO_URL, rsp.getDisplayInfoUrl())
                        .put("ecus", jaEcus);
                UpdateSession s = new UpdateSession(joRoot, PrivStatusCode.OK.equals(statusCode));
                Logger.debug(CALL_TAG + "DATA : %1s", s.toString());
                return s;
            }
        } catch (Exception e) {
            Logger.error(e);
        }
        return null;
    }

    @Override
    public VehicleInfo queryVehicleDetail(int flag) {
        final String CALL_TAG = LogUtil.TAG_RPC_MDA + "[INFO] ";
        Logger.info(CALL_TAG + "%1d", flag);
        MasterDownloadAgent.QueryReq.Builder builder = MasterDownloadAgent.QueryReq.newBuilder();
        if(1 == flag) {
            builder.setAction(MasterDownloadAgent.QueryReq.Action.VEHICLE);
        } else if(2 == flag) {
            builder.setAction(MasterDownloadAgent.QueryReq.Action.VERSION);
        } else {
            builder.setAction(MasterDownloadAgent.QueryReq.Action.ALL);
        }
        builder.setTag(CALL_TAG);
        PrivReqHelper.Response resp = PrivReqHelper.doPost(mBaseUrl + "/query", builder.build().toByteArray());
        Logger.info(CALL_TAG + "RSP : %1d", resp.getStatusCode());
        if(PrivStatusCode.OK.equals(resp.getStatusCode())) {
            try {
                MasterDownloadAgent.QueryRsp rsp = MasterDownloadAgent.QueryRsp.parseFrom(resp.getBody());
                VehicleInfo detail = new VehicleInfo(rsp.getVin(), rsp.getModel(), rsp.getBrand());
                for(int i = 0; i < rsp.getInfoCount(); i++) {
                    MasterDownloadAgent.QueryRsp.EcuInfo info = rsp.getInfo(i);
                    EcuInfo ei = new EcuInfo(info.getName());
                    ei.sn = info.getSn();
                    ei.hwVer = info.getHardware();
                    ei.swVer = info.getSoftware();
                    ei.mProps = JsonHelper.parseObject(info.getDesc());
                    detail.addEcuDetail(ei);
                }
                Logger.debug(CALL_TAG + "DATA : %1s", detail.toString());
                return detail;
            } catch (Exception e) {
                Logger.error(e);
            }
        }
        return null;
    }

    @Override
    public InstallProgress queryUpdateStatus() {
        final String CALL_TAG = LogUtil.TAG_RPC_MDA + "[INS-RET] ";
        Logger.info(CALL_TAG);
        PrivReqHelper.Response resp = PrivReqHelper.doGet(mBaseUrl + "/result", null);
        Logger.info(CALL_TAG + "RSP : %1d", resp.getStatusCode());
        if(PrivStatusCode.OK.equals(resp.getStatusCode())) {
            try {
                MasterDownloadAgent.UpgradeResultRsp rsp = MasterDownloadAgent.UpgradeResultRsp.parseFrom(resp.getBody());
                JSONArray jaEcus = new JSONArray();
                for(MasterDownloadAgent.UpgradeResultRsp.Task t : rsp.getTasksList()) {
                    jaEcus.put(new JSONObject()
                            .put("name", t.getName())
                            .put("pg", t.getProgress())
                            .put("state", getState(t.getStatus())));
                }
                JSONObject joRoot = new JSONObject()
                        .put("ret", jaEcus)
                        .put("state", getState(rsp.getStatus()))
                        .put("usid", rsp.getUsid())
                        .put("target", getTarget(rsp.getStep()));
                InstallProgress insPg = InstallProgress.create(joRoot);
                Logger.debug(CALL_TAG + "DATA : %1s", insPg.toString());
                return insPg;
            } catch (Exception e) {
                Logger.error(e);
            }
        }
        return null;
    }

    private String getTarget(MasterDownloadAgent.UpgradeStep step) {
        switch (step) {
            case SLAVE:
                return "slave";
            case MASTER:
            case UI:
                return "master";
        }
        return null;
    }
    private int getState(MasterDownloadAgent.UpgradeResultRsp.Status status) {
        switch (status) {
            case UPGRADE:
                return ClientState.UPGRADE_STATE_UPGRADE;
            case SUCCESS:
                return ClientState.UPGRADE_STATE_SUCCESS;
            case ROLLBACK:
                return ClientState.UPGRADE_STATE_ROLLBACK;
            case ERROR:
                return ClientState.UPGRADE_STATE_ERROR;
            case FAILURE:
                return ClientState.UPGRADE_STATE_FAILURE;
            default:
                return ClientState.UPGRADE_STATE_IDLE;
        }
    }

    private boolean triggerUpgrade(String usid, MasterDownloadAgent.UpgradeStep step) throws InterruptedException{
        final String CALL_TAG = LogUtil.TAG_RPC_MDA + "[INS-TRIG] ";
        Logger.info(CALL_TAG + "%1d", step.getNumber());
        MasterDownloadAgent.UpgradeReq.Builder builder = MasterDownloadAgent.UpgradeReq.newBuilder()
                .setTag(ReqTag.TAG_SRC_CORE)
                .setUsid(usid)
                .setStep(step);

        PrivReqHelper.Response resp = PrivReqHelper.doPost(mBaseUrl + "/upgrade", builder.build().toByteArray());
        int responseCode = resp.getStatusCode();
        Logger.info(CALL_TAG + "RSP : %1d", resp.getStatusCode());
        if (PrivStatusCode.OK.equals(responseCode) || PrivStatusCode.REQ_SEQ_TRIGGER.equals(responseCode)) {
            return true;
        }
        if(resp.isInterrupted()) {
            throw new InterruptedException("Interrupted in REQUEST");
        }
        return false;
    }

    @Override
    public boolean upgradeEcuInSlave(String usid) throws InterruptedException {
        return triggerUpgrade(usid, MasterDownloadAgent.UpgradeStep.SLAVE);
    }

    @Override
    public boolean upgradeEcuInMaster(String usid) throws InterruptedException {
        return triggerUpgrade(usid, MasterDownloadAgent.UpgradeStep.MASTER);
    }

    private boolean triggerDownload(String usid, MasterDownloadAgent.DownloadReq.Action action) throws InterruptedException{
        final String CALL_TAG = LogUtil.TAG_RPC_MDA + "[DL-TRIG] ";
        Logger.info(CALL_TAG + "%1s @ %2d", usid, action.getNumber());
        MasterDownloadAgent.DownloadReq.Builder builder = MasterDownloadAgent.DownloadReq.newBuilder()
                .setTag(ReqTag.TAG_SRC_CORE)
                .setAction(action);

        if(null != usid) {
            builder.setUsid(usid);
        }

        PrivReqHelper.Response resp = PrivReqHelper.doPost(mBaseUrl + "/download", builder.build().toByteArray());
        Logger.info(CALL_TAG + "RSP : %1d", resp.getStatusCode());
        int responseCode = resp.getStatusCode();
        if (PrivStatusCode.OK.equals(responseCode)) {
            return true;
        }
        if(resp.isInterrupted()) {
            throw new InterruptedException("Interrupted in REQUEST");
        }
        return false;
    }
    @Override
    public boolean downloadPackageStart(String usid) {
        try {
            return triggerDownload(usid, MasterDownloadAgent.DownloadReq.Action.START);
        } catch (InterruptedException e) {
            // do nothing
        }
        return false;
    }

    @Override
    public boolean downloadPackageStop() {
        try {
            return triggerDownload(null, MasterDownloadAgent.DownloadReq.Action.STOP);
        } catch (InterruptedException e) {
            // do nothing
        }
        return false;
    }

    @Override
    public DownloadProgress downloadPackageQuery() throws InterruptedException{
        final String CALL_TAG = LogUtil.TAG_RPC_MDA + "[DL-PG] ";
        Logger.info(CALL_TAG);
        MasterDownloadAgent.DownloadReq.Builder builder = MasterDownloadAgent.DownloadReq.newBuilder()
                .setTag(ReqTag.TAG_SRC_CORE)
                .setAction(MasterDownloadAgent.DownloadReq.Action.QUERY);

        PrivReqHelper.Response resp = PrivReqHelper.doPost(mBaseUrl + "/download", builder.build().toByteArray());
        int responseCode = resp.getStatusCode();
        Logger.info(CALL_TAG + "RSP : %1d", resp.getStatusCode());
        if (PrivStatusCode.OK.equals(responseCode)) {
            try {
                DownloadProgress pg = new DownloadProgress(MasterDownloadAgent.DownloadRsp.parseFrom(resp.getBody()));
                Logger.debug(CALL_TAG + "DATA : %1s", pg.toString());
                return pg;
            } catch (InvalidProtocolBufferException e) {
                Logger.error(e);
            }
        }
        if(resp.isInterrupted()) {
            throw new InterruptedException("Interrupted in REQUEST");
        }
        return null;
    }

    @Override
    public boolean checkAlive() {
        final String CALL_TAG = LogUtil.TAG_RPC_MDA + "[TEST-TRY] ";
        Logger.info(CALL_TAG);
        MasterDownloadAgent.TestReq.Builder builder = MasterDownloadAgent.TestReq.newBuilder()
                .setTag(ReqTag.TAG_SRC_CORE)
                .setType(MasterDownloadAgent.TestReq.Case.ALIVE);
        PrivReqHelper.Response response = PrivReqHelper.doPost(mBaseUrl + "/test", builder.build().toByteArray());
        Logger.info(CALL_TAG + "RSP : %1d", response.getStatusCode());
        return PrivStatusCode.OK.equals(response.getStatusCode());
    }

    @Override
    public boolean checkSystemReady(List<String> lostEcus) {
        final String CALL_TAG = LogUtil.TAG_RPC_MDA + "[TEST-READY] ";
        Logger.info(CALL_TAG);
        MasterDownloadAgent.TestReq.Builder builder = MasterDownloadAgent.TestReq.newBuilder()
                .setTag(ReqTag.TAG_SRC_CORE)
                .setType(MasterDownloadAgent.TestReq.Case.RPC);
        PrivReqHelper.Response response = PrivReqHelper.doPost(mBaseUrl + "/test", builder.build().toByteArray());
        Logger.info(CALL_TAG + "RSP : %1d", response.getStatusCode());
        if(PrivStatusCode.OK.equals(response.getStatusCode())) {
            try {
                MasterDownloadAgent.TestRsp rsp = MasterDownloadAgent.TestRsp.parseFrom(response.getBody());
                if(null != lostEcus) {
                    lostEcus.clear();
                    lostEcus.addAll(rsp.getDataList());
                }
                if(rsp.getDataCount() > 0) {
                    Logger.debug(CALL_TAG + "DATA : %1s", TextUtils.join("; ", lostEcus));
                }
                return rsp.getDataCount() == 0;
            } catch (Exception e) {
                Logger.error(e);
            }
        }
        return false;
    }

    @Override
    public MDAInfo syncMasterStatus() {
        final String CALL_TAG = LogUtil.TAG_RPC_MDA + "[SYNC-ST] ";
        Logger.info(CALL_TAG);
        PrivReqHelper.Response resp = PrivReqHelper.doGet(mBaseUrl + "/sync", null);
        Logger.info(CALL_TAG + "RSP : %1d", resp.getStatusCode());
        if (PrivStatusCode.OK.equals(resp.getStatusCode())) {
            try {
                MDAInfo info = new MDAInfo(MasterDownloadAgent.SyncRsp.parseFrom(resp.getBody()));
                Logger.debug(CALL_TAG + "DATA : %1s", info.toString());
                return info;
            } catch (Exception e) {
                Logger.error(e);
            }
        }
        return null;
    }

    @Override
    public List<String> syncUpdateCondition() {
        final String CALL_TAG = LogUtil.TAG_RPC_MDA + "[SYNC-CDT] ";
        Logger.info(CALL_TAG);
        PrivReqHelper.Response resp = PrivReqHelper.doGet(mBaseUrl + "/sync", null);
        Logger.info(CALL_TAG + "RSP : %1d", resp.getStatusCode());
        if (PrivStatusCode.OK.equals(resp.getStatusCode())) {
            try {
                List<String> cdt = MasterDownloadAgent.SyncRsp.parseFrom(resp.getBody()).getEnvironmentList();
                Logger.debug(CALL_TAG + "DATA : %1s", TextUtils.join("; ", cdt));
                return cdt;
            } catch (Exception e) {
                Logger.error(e);
            }
        }
        return null;
    }

    @Override
    public String syncMasterEnvm() {
        final String CALL_TAG = LogUtil.TAG_RPC_MDA + "[SYNC-ENVM] ";
        Logger.info(CALL_TAG);
        String action = IActionMDA.ENV_ACTION_DEFAULT;
        PrivReqHelper.Response resp = PrivReqHelper.doGet(mBaseUrl + "/sync", null);
        Logger.info(CALL_TAG + "RSP : %1d", resp.getStatusCode());
        if (PrivStatusCode.OK.equals(resp.getStatusCode())) {
            try {
                if (MasterDownloadAgent.SyncRsp.parseFrom(resp.getBody()).getAction() == MasterDownloadAgent.SyncRsp.EnvmAction.UAT) {
                    action = IActionMDA.ENV_ACTION_UAT;
                } else {
                    action = IActionMDA.ENV_ACTION_DEFAULT;
                }
                Logger.debug(CALL_TAG + "DATA : %1s", action);
                return action;
            } catch (Exception e) {
                Logger.error(e);
            }
        }
        return action;
    }

    @Override
    public boolean setMasterEnvm(boolean isUat) {
        final String CALL_TAG = LogUtil.TAG_RPC_MDA + "[ENVM-SET] ";
        Logger.info(CALL_TAG + "%1b", isUat);
        MasterDownloadAgent.EnvmReq.Builder builder = MasterDownloadAgent.EnvmReq.newBuilder();
        builder.setTag("space");
        if (isUat) {
            builder.setAction(MasterDownloadAgent.EnvmReq.EnvmAction.UAT);
        } else {
            builder.setAction(MasterDownloadAgent.EnvmReq.EnvmAction.PRODUCE);
        }
        PrivReqHelper.Response resp = PrivReqHelper.doPost(mBaseUrl + "/environment", builder.build().toByteArray());
        Logger.info(CALL_TAG + "RSP : %1d", resp.getStatusCode());
        return PrivStatusCode.OK.equals(resp.getStatusCode());
    }

    @Override
    public boolean sendPointData(int type, long time, String msg) {
        try {
            final String CALL_TAG = LogUtil.TAG_RPC_MDA + "[SEND-POINT] ";
            Logger.info(CALL_TAG + "type:%d,msg:%s", type, msg);
            MasterDownloadAgent.EventReq.Point.Builder point = MasterDownloadAgent.EventReq.Point.newBuilder()
                    .setId(type)
                    .setAt(time)
                    .setMsg(TextUtils.isEmpty(msg)?"":msg);
            MasterDownloadAgent.EventReq.Builder builder = MasterDownloadAgent.EventReq.newBuilder()
                    .setTag(ReqTag.TAG_SRC_CORE)
                    .setAction(MasterDownloadAgent.EventReq.Action.POINT)
                    .addPoint(point);
            PrivReqHelper.Response response = PrivReqHelper.doPost(mBaseUrl + "/event", builder.build().toByteArray());
            Logger.info(CALL_TAG + "RSP : %1d", response.getStatusCode());
            return PrivStatusCode.OK.equals(response.getStatusCode());
        } catch (Exception e) {
            Logger.error(e);
        }
        return false;
    }

    @Override
    public boolean sendEventData(long time, int upgradeType, int code, String msg, int result, String scheduleID, int eic) {
        try {
            final String CALL_TAG = LogUtil.TAG_RPC_MDA + "[SEND-EVENT] ";
            Logger.info(CALL_TAG + "type:%d,code:%d,result:%d,msg:%s", upgradeType, code, result, msg);
            MasterDownloadAgent.EventReq.Event.Builder event = MasterDownloadAgent.EventReq.Event.newBuilder()
                    .setAt(time)
                    .setUpgradeType(upgradeType)
                    .setEventCode(code)
                    .setResult(result)
                    .setScheduleId(scheduleID)
                    .setEICSystem(eic)
                    .setMsg(TextUtils.isEmpty(msg) ? "" : msg);
            MasterDownloadAgent.EventReq.Builder builder = MasterDownloadAgent.EventReq.newBuilder()
                    .setTag(ReqTag.TAG_SRC_CORE)
                    .setAction(MasterDownloadAgent.EventReq.Action.EVENT)
                    .addEvent(event);
            PrivReqHelper.Response response = PrivReqHelper.doPost(mBaseUrl + "/event", builder.build().toByteArray());
            Logger.info(CALL_TAG + "RSP : %1d", response.getStatusCode());
            return PrivStatusCode.OK.equals(response.getStatusCode());
        } catch (Exception e) {
            Logger.error(e);
        }
        return false;
    }

    public boolean sendFotaV2Data(String ecu, int state, int ecustate, int code, String erMsg, long time) {
        final String CALL_TAG = LogUtil.TAG_RPC_MDA + "[SEND-FOTA] ";
        try {
            Logger.info(CALL_TAG + "Ecu:%s,state:%d,ecustate:%d,code:%d,msg:%s", ecu, state, ecustate, code, erMsg);
            MasterDownloadAgent.EventReq.Fota.Builder fota = MasterDownloadAgent.EventReq.Fota.newBuilder()
                    .setEcu(TextUtils.isEmpty(ecu) ? "" : ecu)
                    .setTotalState(state)
                    .setState(ecustate)
                    .setTime(time)
                    .setCode(code)
                    .setError(TextUtils.isEmpty(erMsg)?"":erMsg);
            MasterDownloadAgent.EventReq.Builder builder = MasterDownloadAgent.EventReq.newBuilder()
                    .setTag(ReqTag.TAG_SRC_CORE)
                    .setAction(MasterDownloadAgent.EventReq.Action.FOTA)
                    .addFota(fota);
            Logger.info(CALL_TAG);
            PrivReqHelper.Response response = PrivReqHelper.doPost(mBaseUrl + "/event", builder.build().toByteArray());
            Logger.info(CALL_TAG + "RSP : %1d", response.getStatusCode());
            return PrivStatusCode.OK.equals(response.getStatusCode());
        } catch (Exception e) {
            Logger.error(e);
        }
        return false;
    }

    @Override
    public int fireRescue(String action, Bundle bundle) {
        MasterDownloadAgent.RescueReq.Builder req = MasterDownloadAgent.RescueReq.newBuilder();
        req.setTag("space");
        switch (action) {
            case IActionMDA.EVENT_RESCUE_QUERY:
                req.setEvent(MasterDownloadAgent.RescueReq.Event.QUERY);
                break;
            case IActionMDA.EVENT_RESCUE_VERIFY:
                req.setEvent(MasterDownloadAgent.RescueReq.Event.VERIFY);
                break;
            case IActionMDA.EVENT_RESCUE_RESULT:
                req.setEvent(MasterDownloadAgent.RescueReq.Event.RESULT);
                break;
        }
        PrivReqHelper.Response resp = PrivReqHelper.doPost(mBaseUrl + "/rescue", req.build().toByteArray());
        Logger.info("fireRescue rsp code:" + resp.getStatusCode());
        if(resp.getStatusCode() == PrivStatusCode.OK.getStatusCode()) {
            try {
                int ret = MasterDownloadAgent.RescueResp.parseFrom(resp.getBody()).getResult();
                Logger.info("fireRescue rsp result:" + ret);
                return ret;
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    @Override
    public boolean eCallEvent() {
        MasterDownloadAgent.EcallReq.Builder req = MasterDownloadAgent.EcallReq.newBuilder();
        Logger.info("E-Call event send to mda");
        PrivReqHelper.Response resp = PrivReqHelper.doPost(mBaseUrl + "/ecall", req.build().toByteArray());
        Logger.info("eCallEvent rsp code:" + resp.getStatusCode());
        return resp.getStatusCode() == PrivStatusCode.OK.getStatusCode();
    }

    @Override
    public boolean updateTimeOut() {
        MasterDownloadAgent.TimeOutReq.Builder req = MasterDownloadAgent.TimeOutReq.newBuilder();
        Logger.info("update time out event send to mda");
        PrivReqHelper.Response resp = PrivReqHelper.doPost(mBaseUrl + "/timeout", req.build().toByteArray());
        Logger.info("update time out rsp code:" + resp.getStatusCode());
        return resp.getStatusCode() == PrivStatusCode.OK.getStatusCode();
    }

}
