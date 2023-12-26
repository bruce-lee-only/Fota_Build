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

import com.carota.build.ParamDM;
import com.carota.build.ParamMDA;
import com.carota.mda.remote.info.DownloadInfo;
import com.carota.mda.remote.info.IDownloadStatus;
import com.carota.protobuf.DownloadManager;
import com.carota.svr.PrivReqHelper;
import com.carota.svr.PrivStatusCode;
import com.carota.util.LogUtil;
import com.carota.util.ReqTag;
import com.momock.util.Logger;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ActionDM implements IActionDM, IDownloadStatus {

    private final Map<String, IDownloadInfo> mTasks;
    private final String mBaseUrl;
    private final String mName;

    public ActionDM(String name, String host, int port) {
        mName = name;
        if (port > 0) {
            mBaseUrl = "http://" + host + ":" + port;
        } else {
            mBaseUrl = "http://" + host;
        }
        mTasks = new HashMap<>();
    }

    public ActionDM(String name, String host) {
        this(name, host, 0);
    }

    @Override
    public String toString() {
        return mName;
    }

    @Override
    public boolean download(String id, String url, String md5, String desc) {
        final String CALL_TAG = LogUtil.TAG_RPC_DM + "[START] ";
        Logger.info(CALL_TAG + "%1s : %2s", mName, id);
        DownloadManager.DownloadReq.Builder req = DownloadManager.DownloadReq.newBuilder()
                .setTag(ReqTag.TAG_SRC_MDA)
                .addPlans(DownloadManager.DownloadReq.Plan.newBuilder()
                        .setId(id)
                        .setUrl(url)
                        .setMd5(md5)
                        .setDesc(desc)
                );
        PrivReqHelper.Response response = PrivReqHelper.doPost(mBaseUrl + "/dl", req.build().toByteArray());
        Logger.info(CALL_TAG + "RSP : %1d", response.getStatusCode());
        return PrivStatusCode.OK.getStatusCode() == response.getStatusCode();
    }

    @Override
    public IDownloadStatus queryStatus() {
        final String CALL_TAG = LogUtil.TAG_RPC_DM + "[PG] ";
        Logger.info(CALL_TAG + "%1s", mName);
        PrivReqHelper.Response response = PrivReqHelper.doGet(mBaseUrl + "/pg", null);
        Logger.info(CALL_TAG + "RSP : %1d", response.getStatusCode());
        if (PrivStatusCode.OK.getStatusCode() == response.getStatusCode()) {
            try {
                DownloadManager.ProgressRsp rsp = DownloadManager.ProgressRsp.parseFrom(response.getBody());
                for (DownloadManager.ProgressRsp.Work w : rsp.getWorksList()) {
                    DownloadInfo info = new DownloadInfo(w);
                    mTasks.put(w.getId(), info);
                    Logger.debug(CALL_TAG + "DATA : %1s", info.toString());
                }
            } catch (Exception e) {
                Logger.error(e);
            }
        }
        return this;
    }

    @Override
    public boolean stop(String id) {
        final String CALL_TAG = LogUtil.TAG_RPC_DM + "[STOP] ";
        Logger.info(CALL_TAG + "%1s : %2s", mName, id);
        DownloadManager.CommandReq.Builder req = DownloadManager.CommandReq.newBuilder()
                .setTag(ReqTag.TAG_SRC_MDA)
                .setAction(DownloadManager.CommandReq.Action.STOP);
        if (null != id) {
            req.addIds(id);
        }
        PrivReqHelper.Response response = PrivReqHelper.doPost(mBaseUrl + "/cmd", req.build().toByteArray());
        Logger.info(CALL_TAG + "RSP : %1d", response.getStatusCode());
        return PrivStatusCode.OK.getStatusCode() == response.getStatusCode();
    }

    @Override
    public boolean delete(String id) {
        final String CALL_TAG = LogUtil.TAG_RPC_DM + "[DEL] ";
        Logger.info(CALL_TAG + "%1s : %2s", mName, id);
        DownloadManager.CommandReq.Builder req = DownloadManager.CommandReq.newBuilder()
                .setTag(ReqTag.TAG_SRC_MDA)
                .setAction(DownloadManager.CommandReq.Action.DELETE);
        if (null != id) {
            req.addIds(id);
        }
        PrivReqHelper.Response response = PrivReqHelper.doPost(mBaseUrl + "/cmd", req.build().toByteArray());
        Logger.info(CALL_TAG + "RSP : %1d", response.getStatusCode());
        mTasks.clear();
        return PrivStatusCode.OK.getStatusCode() == response.getStatusCode();
    }

    @Override
    public boolean clean(long size, List<String> id) {
        final String CALL_TAG = LogUtil.TAG_RPC_DM + "[CLEAN] ";
        Logger.info(CALL_TAG + "%1s : %2s", mName, TextUtils.join("; ", id));
        DownloadManager.CommandReq.Builder req = DownloadManager.CommandReq.newBuilder()
                .setTag(ReqTag.TAG_SRC_MDA)
                .setAction(DownloadManager.CommandReq.Action.PREPARE)
                .setExtra("" + size)
                .addAllIds(null == id ? new ArrayList<>() : id);
        PrivReqHelper.Response response = PrivReqHelper.doPost(mBaseUrl + "/cmd", req.build().toByteArray());
        Logger.info(CALL_TAG + "RSP : %1d", response.getStatusCode());
        try {
            DownloadManager.CommandRsp resp = DownloadManager.CommandRsp.parseFrom(response.getBody());
            return (resp.getFree() - resp.getRequire() > 0) & PrivStatusCode.OK.getStatusCode() == response.getStatusCode();
        } catch (Exception e) {
            Logger.error(e);
        }
        return false;
    }

    @Override
    public IDownloadInfo getDownloadInfo(String id) {
        return mTasks.get(id);
    }

    @Override
    public List<IDownloadInfo> listDownloadInfo() {
        return new ArrayList<>(mTasks.values());
    }

    public static boolean pullFile(String targetHost, String fileId, File out) {
        final String CALL_TAG = LogUtil.TAG_RPC_DM + "[PULL] ";
        String url = "http://" + targetHost + "/file?id=" + fileId;
        boolean ret = PrivReqHelper.doDownload(url, out);
        Logger.info(CALL_TAG + "%1s : %2s @ %3b", targetHost, fileId, ret);
        return ret;
    }

    public static InputStream openInputStream(String targetHost, String fileId) {
        final String CALL_TAG = LogUtil.TAG_RPC_DM + "[INPUT] ";
        String url = "http://" + targetHost + "/file?id=" + fileId;
        InputStream ret = PrivReqHelper.getInputStream(url);
        Logger.info(CALL_TAG + "%1s : %2s", targetHost, fileId);
        return ret;
    }

    public static long getFileLength(String targetHost, String fileId) {
        final String CALL_TAG = LogUtil.TAG_RPC_DM + "[LENGTH] ";
        String url = "http://" + targetHost + "/file?id=" + fileId;
        long len = PrivReqHelper.getFileLength(url);
        Logger.info(CALL_TAG + "%1s : %2s", targetHost, fileId);
        return len;
    }
}
