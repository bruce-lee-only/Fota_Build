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

import android.text.TextUtils;

import com.carota.core.remote.info.HubInfo;
import com.carota.protobuf.ServiceHub;
import com.carota.svr.PrivReqHelper;
import com.carota.svr.PrivStatusCode;
import com.carota.util.LogUtil;
import com.carota.util.ReqTag;
import com.google.protobuf.InvalidProtocolBufferException;
import com.momock.util.Logger;

import java.util.List;

public class ActionSH implements IActionSH {

    private final String mHubHost;
    private ServiceHub.RegisterReq mReqPayload;

    public ActionSH(String hubHost) {
        if(TextUtils.isEmpty(hubHost)) {
            mHubHost = null;
        } else {
            mHubHost = hubHost;
        }
        mReqPayload = null;
    }

    // http://ota_proxy/register?p=8090&m=ota_master,ota_dm,ota_test
    public static byte[] createRegisterData(int port, List<String> moduleList) {
        if(port > 0 && null != moduleList && !moduleList.isEmpty()) {
            return ServiceHub.RegisterReq.newBuilder()
                    .setTag(ReqTag.TAG_SRC_CORE)
                    .addAllModules(moduleList)
                    .setPort(port).build().toByteArray();
        }
        return null;
    }

//    public String createRegisterUrl(int port, List<String> moduleList) {
//        return ActionSH.createRegisterUrl(mHubHost, port, moduleList);
//    }
//
//    public boolean registerService(int port, List<String> moduleList) {
//        return registerService(createRegisterUrl(port, moduleList));
//    }

    public boolean registerService() {
        if (null != mHubHost && null != mReqPayload) {
            PrivReqHelper.Response resp = PrivReqHelper.doPost("http://" + mHubHost + "/register", mReqPayload.toByteArray());
            return PrivStatusCode.OK.equals(resp.getStatusCode());
        }
        return false;
    }

    @Override
    public HubInfo queryInfo() {
        if (null != mHubHost) {
            final String CALL_TAG = LogUtil.TAG_RPC_SH + "[QUERY] ";
            Logger.info(CALL_TAG);
            PrivReqHelper.Response resp = PrivReqHelper.doGet("http://" + mHubHost + "/info", null);
            Logger.info(CALL_TAG + "RSP : %1d", resp.getStatusCode());
            if(PrivStatusCode.OK.equals(resp.getStatusCode())) {
                try {
                    HubInfo info = new HubInfo(ServiceHub.InfoRsp.parseFrom(resp.getBody()));
                    Logger.debug(CALL_TAG + "DATA : %1s", info.toString());
                    return info;
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}
