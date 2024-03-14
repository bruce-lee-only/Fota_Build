/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.mda.remote;

import android.text.TextUtils;

import com.carota.mda.remote.info.BomInfo;
import com.carota.mda.remote.info.EcuInfo;
import com.carota.mda.remote.info.SlaveInstallResult;
import com.carota.protobuf.SlaveDownloadAgent;
import com.carota.svr.PrivReqHelper;
import com.carota.svr.PrivStatusCode;
import com.carota.util.LogUtil;
import com.carota.util.ReqTag;
import com.google.protobuf.InvalidProtocolBufferException;
import com.momock.util.FileHelper;
import com.momock.util.JsonHelper;
import com.momock.util.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Slave Download Agent
 */

public class ActionSDA implements IActionSDA {

    @Override
    public EcuInfo queryInfo(String host, String name, BomInfo bomInfo) {
        final String CALL_TAG = LogUtil.TAG_RPC_SDA + "[QUERY] ";
        Logger.info(CALL_TAG + "%1s @ %2s", name, host);

        EcuInfo info = new EcuInfo(name);

        String url = "http://" + host + "/info";
        PrivReqHelper.Response resp = new PrivReqHelper.Response();
        if (bomInfo != null) {
            resp = PrivReqHelper.doPost(url, bomInfo.getInfoReq().build().toByteArray());
        } else {
            SlaveDownloadAgent.InfoReq.Builder req = SlaveDownloadAgent.InfoReq.newBuilder()
                    .setTag(ReqTag.TAG_SRC_MDA)
                    .setName(name);
            resp = PrivReqHelper.doPost(url, req.build().toByteArray());
        }
        Logger.info(CALL_TAG + "RSP : %1d", resp.getStatusCode());
        if (PrivStatusCode.OK.equals(resp.getStatusCode())) {
            try {
                SlaveDownloadAgent.InfoRsp rsp = SlaveDownloadAgent.InfoRsp.parseFrom(resp.getBody());
                info.swVer = rsp.getSoftware();
                info.hwVer = rsp.getHardware();
                info.sn = rsp.getSn();
                JSONObject extra = JsonHelper.parseObject(rsp.getExtra());
                info.mProps = null == extra ? new JSONObject() : extra;
                Logger.debug(CALL_TAG + "DATA : %1s", info.toString());
            } catch (InvalidProtocolBufferException e) {
                Logger.error(e);
            }
        }
        return TextUtils.isEmpty(info.swVer) ? null : info;
    }

    /**
     * 并行刷写前置动作
     *
     * @param list
     * @return
     */
    @Override
    public boolean prepareInstall(String host, List<BomInfo> list) {
        final String CALL_TAG = LogUtil.TAG_RPC_SDA + "[PTRIG] ";
        try {
            Logger.info(CALL_TAG + "%1s ,size:%2d", host, list.size());
            SlaveDownloadAgent.InstallPrepareReq.Builder req = BomInfo.getInstallPrepareReq(list);
            PrivReqHelper.Response resp = PrivReqHelper.doPost("http://" + host + "/prepareinstall", req.build().toByteArray());
            Logger.info(CALL_TAG + "RSP : %1d", resp.getStatusCode());
            if (PrivStatusCode.OK.equals(resp.getStatusCode())) {
                SlaveDownloadAgent.InstallPrepareResp parse = SlaveDownloadAgent.InstallPrepareResp.parseFrom(resp.getBody());
                Logger.debug(CALL_TAG + "DATA : %1d", parse.getCode());
                return parse.getCode() == 0;
            }
        } catch (Exception e) {
            Logger.error(e);
        }
        return false;
    }

    @Override
    public int triggerInstall(String host, String dmHost, String ecuName, int domain,
                              String tgtId, String tgtVer,
                              String srcId, String srcVer,  String tgtSignId, String srcSignId, BomInfo bomInfo) {
        try {
            final String CALL_TAG = LogUtil.TAG_RPC_SDA + "[TRIG] ";
            Logger.info(CALL_TAG + "%1s @ %2s : DST = %3s; SRC = %4s", ecuName, host, tgtVer, srcVer);
            SlaveDownloadAgent.InstallReq.Payload.Builder target, source;
            target = SlaveDownloadAgent.InstallReq.Payload.newBuilder()
                    .setFile(tgtId)
                    .setId(tgtId)
                    .setVer(tgtVer);
            if (tgtSignId != null) {
                target.setSign(tgtSignId);
            }
            if (null != srcId && null != srcVer) {
                source = SlaveDownloadAgent.InstallReq.Payload.newBuilder()
                        .setId(srcId)
                        .setFile(srcId)
                        .setVer(srcVer);
                if(srcSignId != null) {
                    source.setSign(srcSignId);
                }
            } else {
                source = null;
            }

            SlaveDownloadAgent.InstallReq.Builder req = SlaveDownloadAgent.InstallReq.newBuilder()
                    .setTag(ReqTag.TAG_SRC_MDA)
                    .setHost(dmHost)
                    .setName(ecuName)
                    .setDomain(domain)
                    .addDst(target);
            if(null != source) {
                req.addSrc(source);
            }

            if (bomInfo != null) {
                req.setApplyInfo(bomInfo.getInstallReq());
            }

            PrivReqHelper.Response resp = PrivReqHelper.doPost("http://" + host + "/install", req.build().toByteArray());
            Logger.info(CALL_TAG + "RSP : %1d", resp.getStatusCode());
            if(PrivStatusCode.OK.equals(resp.getStatusCode())) {
                return RET_INS_OK;
            } else if(PrivStatusCode.REQ_SEQ_TRIGGER.getStatusCode() == resp.getStatusCode()) {
                return RET_INS_BUSY;
            } else {
                return RET_INS_FAIL;
            }
        } catch (Exception e) {
            Logger.error(e);
        }

        return RET_INS_FAIL;
    }

    @Override
    public SlaveInstallResult queryInstallResult(String host, String ecu) {
        try {
            final String CALL_TAG = LogUtil.TAG_RPC_SDA + "[RET] ";
            Logger.info(CALL_TAG + "%1s @ %2s", ecu, host);
            SlaveDownloadAgent.ResultReq.Builder req = SlaveDownloadAgent.ResultReq.newBuilder()
                    .setTag(ReqTag.TAG_SRC_MDA)
                    .addName(ecu);

            PrivReqHelper.Response resp = PrivReqHelper.doPost("http://" + host + "/result", req.build().toByteArray());
            Logger.info(CALL_TAG + "RSP : %1d", resp.getStatusCode());
            if (PrivStatusCode.OK.equals(resp.getStatusCode())) {
                SlaveInstallResult result = new SlaveInstallResult(SlaveDownloadAgent.ResultRsp.parseFrom(resp.getBody()));
                Logger.debug(CALL_TAG + "DATA : %1s", result.toString());
                return result;
            }
        } catch (Exception e) {
            Logger.error(e);
        }
        return null;
    }

    @Override
    public boolean uploadEvent(String host, String type, int max, IOnUploadEvent cb) {
        final String CALL_TAG = LogUtil.TAG_RPC_SDA + "[EVT] ";
        Logger.info(CALL_TAG + "%1s : TP = %2s; MAX = %3d", host, type, max);
        SlaveDownloadAgent.EventReq.Builder reqFetch = SlaveDownloadAgent.EventReq.newBuilder()
                .setTag(ReqTag.TAG_SRC_MDA)
                .setAction(SlaveDownloadAgent.EventReq.Action.FETCH)
                .setSize(Math.max(max, 0))
                .setType(null == type ? "" : type);
        PrivReqHelper.Response resp = PrivReqHelper.doPost("http://" + host + "/event", reqFetch.build().toByteArray());
        Logger.info(CALL_TAG + "RSP : %1d", resp.getStatusCode());
        if (PrivStatusCode.OK.equals(resp.getStatusCode())) {
            try {
                SlaveDownloadAgent.EventRsp rsp = SlaveDownloadAgent.EventRsp.parseFrom(resp.getBody());

                SlaveDownloadAgent.EventReq.Builder reqDel = SlaveDownloadAgent.EventReq.newBuilder()
                        .setTag(ReqTag.TAG_SRC_MDA)
                        .setAction(SlaveDownloadAgent.EventReq.Action.DELETE);

                List<String> payload = new ArrayList<>();

                for(SlaveDownloadAgent.EventRsp.Event evt : rsp.getEventsList()) {
                    reqDel.addIds(evt.getId());
                    payload.add(evt.getData());
                }
                Logger.debug(CALL_TAG + "SEND : " + TextUtils.join("; ", payload));
                if (cb.send(payload)) {
                    Logger.debug(CALL_TAG + "CLEAN");
                    PrivReqHelper.doPost("http://" + host + "/event", reqDel.build().toByteArray());
                    return true;
                }
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public boolean collectLogFiles(String host, int max, File dir, String extraPath) {
        final String CALL_TAG = LogUtil.TAG_RPC_SDA + "[PK-LOG] ";
        Logger.info(CALL_TAG + "%1s : MAX = %2d", host, max);
        FileHelper.mkdir(dir);
        try {
            SlaveDownloadAgent.LogReq.Builder req = SlaveDownloadAgent.LogReq.newBuilder()
                    .setTag(ReqTag.TAG_SRC_MDA)
                    .setAction(SlaveDownloadAgent.LogReq.Action.LIST)
                    .setSize(Math.max(max, 0));
            if (!TextUtils.isEmpty(extraPath)) {
                req.setExtraPath(extraPath);
            }
            PrivReqHelper.Response resp = PrivReqHelper.doPost("http://" + host + "/log", req.build().toByteArray());
            Logger.info(CALL_TAG + "RSP : %1d", resp.getStatusCode());
            if (PrivStatusCode.OK.equals(resp.getStatusCode())) {
                SlaveDownloadAgent.LogRsp rsp = SlaveDownloadAgent.LogRsp.parseFrom(resp.getBody());
                JSONArray jArrayResp = new JSONArray(rsp.getFilesList());
                downloadFiles(host, dir, jArrayResp);
            }
        } catch (Exception e) {
            Logger.error(e);
        }
        return true;
    }

    @Override
    public boolean checkAlive(String host) {
        final String CALL_TAG = LogUtil.TAG_RPC_SDA + "[TRY] ";
        Logger.info(CALL_TAG + "%1s", host);
        SlaveDownloadAgent.ResultReq.Builder req = SlaveDownloadAgent.ResultReq.newBuilder()
                .setTag(ReqTag.TAG_SRC_MDA);

        PrivReqHelper.Response resp = PrivReqHelper.doPost("http://" + host + "/result", req.build().toByteArray());
        Logger.info(CALL_TAG + "RSP : %1d", resp.getStatusCode());
        return PrivStatusCode.OK.equals(resp.getStatusCode());
    }

    private void downloadFiles(String host, File dir, JSONArray joResp) {
        if (null != joResp) {
            final String CALL_TAG = LogUtil.TAG_RPC_SDA + "[DL-LOG] ";
            for (int i = 0; i < joResp.length(); i++) {
                String id = joResp.optString(i);
                if (!id.isEmpty()) {
                    File target = new File(dir, id);
                    if (target.exists()) {
                        Logger.debug(CALL_TAG + "%1b @ %2s", target.exists(), target);
                        return;
                    }
                    File tmp = new File(dir, id + ".tmp");
                    if (tmp.exists()) {
                        FileHelper.delete(tmp);
                    }
                    boolean download = PrivReqHelper.doDownload("http://" + host + "/log/file" + "?id=" + id, tmp);
                    if (download) {
                        tmp.renameTo(target);
                    }
                    Logger.debug(CALL_TAG + "%1b @ %2s", target.exists(), target);
                }
            }
        }
    }
}
