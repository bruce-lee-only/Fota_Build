/*******************************************************************************
 * Copyright (C) 2018-2022 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.mda.provider;

import android.os.Bundle;

import com.carota.core.VehicleEvent;
import com.carota.protobuf.MasterDownloadAgent;
import com.carota.protobuf.Telemetry;
import com.carota.svr.HttpResp;
import com.carota.svr.PrivStatusCode;
import com.carota.svr.SimpleHandler;
import com.google.protobuf.InvalidProtocolBufferException;
import com.momock.util.Logger;

import java.util.List;
import java.util.Map;

/**
 * Notify Center
 */
public class NotifyCenterHandler extends SimpleHandler {

    public NotifyCenterHandler() {

    }

    @Override
    public HttpResp post(String path, Map<String, List<String>> params, byte[] body, Object extra) {
        PrivStatusCode code = PrivStatusCode.SRV_ACT_UNKNOWN;
        Telemetry.EmptyRsp.Builder builder = Telemetry.EmptyRsp.newBuilder();
        try {
            MasterDownloadAgent.SyncClockReq req = MasterDownloadAgent.SyncClockReq.parseFrom(body);
            long systemClock = req.getSystemClock();
            //处理心跳逻辑
            Bundle bundle = new Bundle();
            bundle.putLong("systemClock", systemClock);
//            if (mIBeatEvent.onBeatEvent(VehicleEventService.HUB_BEAT_PATH,
//                    VehicleEvent.EVENT_BEAT,
//                    bundle)) {
//                code = PrivStatusCode.OK;
//            }
        } catch (InvalidProtocolBufferException e) {
            Logger.error(e);
        }
        return HttpResp.newInstance(code, builder.build().toByteArray());
    }
}
