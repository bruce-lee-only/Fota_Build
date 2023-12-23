/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.vsi.provider;

import android.text.TextUtils;

import com.carota.core.VehicleEvent;
import com.carota.protobuf.Telemetry;
import com.carota.protobuf.VehicleStatusInformation;
import com.carota.svr.HttpResp;
import com.carota.svr.PrivStatusCode;
import com.carota.svr.SimpleHandler;
import com.carota.vsi.VehicleInformation;
import com.momock.util.Logger;

import java.util.List;
import java.util.Map;

public class VehicleEventHandler extends SimpleHandler {

    private VehicleInformation mService;

    public VehicleEventHandler(VehicleInformation service) {
        super();
        mService = service;
    }

    @Override
    public HttpResp post(String path, Map<String, List<String>> params, byte[] body, Object extra) {
        PrivStatusCode code = PrivStatusCode.SRV_ACT_UNKNOWN;
        Telemetry.EmptyRsp.Builder builder = Telemetry.EmptyRsp.newBuilder();
        try {
            VehicleStatusInformation.EventReq req = VehicleStatusInformation.EventReq.parseFrom(body);
            switch (req.getAction()) {
                case FIRE:
                    code = fireEvent(req, builder);
                    break;
                case REMOVE:
                    //code = removeEvent(req, builder);
                    break;
                case REGISTER:
                    //code = registerEvent(req, builder);
                    break;
            }
        } catch (Exception e) {
            Logger.error(e);
        }
        return HttpResp.newInstance(code, builder.build().toByteArray());
    }

    private PrivStatusCode fireEvent(VehicleStatusInformation.EventReq req,
                                     Telemetry.EmptyRsp.Builder rsp) {
        String event = findEventName(req);
        if(!TextUtils.isEmpty(event)) {
            // TODO: impl event data (ON & OFF)
            mService.fireEvent(event, req.getDelay(), null);
            return PrivStatusCode.OK;
        }
        return PrivStatusCode.SRV_ACT_UNKNOWN;
    }

    private String findEventName(VehicleStatusInformation.EventReq req) {
        String event = null;
        switch (req.getEvent()) {
            case MODE_OTA:
                event = VehicleEvent.EVENT_RUNTIME;
                break;
            case SCHEDULE:
                event = VehicleEvent.EVENT_SCHEDULE;
                break;
            case MODE_PWR_OTA:
                event = VehicleEvent.EVENT_ELECTRIC_RUNTIME;
                break;
        }
        return event;
    }
}
