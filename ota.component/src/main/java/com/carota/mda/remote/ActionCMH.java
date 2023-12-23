/*******************************************************************************
 * Copyright (C) 2018-2021 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/
package com.carota.mda.remote;


import com.carota.cmh.CmhUtil;
import com.carota.core.ScheduleAttribute;
import com.carota.protobuf.ControlMessageHandler;
import com.carota.svr.PrivReqHelper;
import com.carota.svr.PrivStatusCode;
import com.carota.util.LogUtil;
import com.google.protobuf.InvalidProtocolBufferException;
import com.momock.util.Logger;

public class ActionCMH implements IActionCMH {
    private final String mHost;

    public ActionCMH(String host) {
        mHost = host;
    }

    @Override
    public boolean setScheduleField(String tag, long time, int type, String tid, String track) {
        final String CALL_TAG = LogUtil.TAG_RPC_CMH + "[SET] ";
        Logger.info(CALL_TAG + "tag:%1s time:%2d type:%3s tid:%4s track:%5s @ %6s", tag, time, type, tid, track, mHost);

        ControlMessageHandler.SetFieldReq req = CmhUtil.buildScheduleField(tag, track, time, type, tid);
        PrivReqHelper.Response resp =
                PrivReqHelper.doPost("http://" + mHost + IActionCMH.GMH_SET_FIELD,
                        req.toByteArray());
        Logger.info(CALL_TAG + "Set ScheduleField RSP : %1d", resp.getStatusCode());

        return PrivStatusCode.OK.equals(resp.getStatusCode());
    }

    @Override
    public ScheduleAttribute getScheduleField(String tag) {
        final String CALL_TAG = LogUtil.TAG_RPC_CMH + "[GET] ";
        Logger.info(CALL_TAG + "tag:%1s @ %2s", tag, mHost);

        ControlMessageHandler.QueryFieldReq.Builder req =
                ControlMessageHandler.QueryFieldReq.newBuilder();
        req.setTag(tag);
        req.setGroup(CmhUtil.GROUP_SCHEDULE);

        PrivReqHelper.Response resp =
                PrivReqHelper.doPost("http://" + mHost + IActionCMH.GMH_GET_FIELD,
                        req.build().toByteArray());
        Logger.info(CALL_TAG + "Get ScheduleField RSP : %1d", resp.getStatusCode());
        if (PrivStatusCode.OK.equals(resp.getStatusCode())) {
            try {
                ControlMessageHandler.QueryFieldRsp rsp = ControlMessageHandler.QueryFieldRsp.parseFrom(resp.getBody());
                return CmhUtil.rspToScheduleAttribute(rsp);
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public boolean setErrorWakeUpField(String tag, int code, String desc, String track) {
        final String CALL_TAG = LogUtil.TAG_RPC_CMH + "[EOR] ";
        Logger.info(CALL_TAG + "tag:%1s code:%2d desc:%3s track:%4s @ %5s", tag, code, desc, track, mHost);
        ControlMessageHandler.SetFieldReq req = CmhUtil.buildErrorWakeUpField(tag, track, code, desc);

        PrivReqHelper.Response resp =
                PrivReqHelper.doPost("http://" + mHost + IActionCMH.GMH_SET_FIELD,
                        req.toByteArray());
        Logger.info(CALL_TAG + "RSP : %1d", resp.getStatusCode());
        return PrivStatusCode.OK.equals(resp.getStatusCode());
    }
}
