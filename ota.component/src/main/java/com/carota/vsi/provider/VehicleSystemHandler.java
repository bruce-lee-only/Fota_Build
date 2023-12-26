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

import com.carota.protobuf.VehicleStatusInformation;
import com.carota.svr.HttpResp;
import com.carota.svr.PrivStatusCode;
import com.carota.svr.SimpleHandler;
import com.carota.vsi.VehicleInformation;
import com.momock.util.Logger;

import java.util.List;
import java.util.Map;

public class VehicleSystemHandler extends SimpleHandler {

    private VehicleInformation mService;

    public VehicleSystemHandler(VehicleInformation service) {
        super();
        mService = service;
    }

    @Override
    public HttpResp post(String path, Map<String, List<String>> params, byte[] body, Object extra) {
        PrivStatusCode code = PrivStatusCode.SRV_ACT_UNKNOWN;
        VehicleStatusInformation.VehicleSysRsp.Builder builder = VehicleStatusInformation.VehicleSysRsp.newBuilder();
        try {
            VehicleStatusInformation.VehicleSysReq req = VehicleStatusInformation.VehicleSysReq.parseFrom(body);
            for(VehicleStatusInformation.VehicleSysReq.Configure cfg : req.getDataList()) {
                switch (cfg.getKey()) {
                    case TIMER_NOTIFY:
                        code = setAttribute(VehicleInformation.ATTR_TIMER_NOTIFY, cfg.getValStr(), builder);
                        break;
                    case OTA_STATE:
                        code = setAttribute(VehicleInformation.ATTR_OTA_STATE, cfg.getValInt(), builder);
                        break;
                    case OTA_TASK:
                        code = setAttribute(VehicleInformation.ATTR_OTA_TASK, cfg.getValStr(), builder);
                        break;
                    case OTA_PROGRESS:
                        code = setAttribute(VehicleInformation.ATTR_OTA_PROGRESS, cfg.getValInt(), builder);
                        break;
                    case OTA_EXTRA:
                        code = setAttribute(VehicleInformation.ATTR_OTA_EXTRA, cfg.getValStr(), builder);
                        break;
                    default:
                        throw new RuntimeException();
                }
            }
        } catch (Exception e) {
            Logger.error(e);
        }
        loadAttr(builder);
        return HttpResp.newInstance(code, builder.build().toByteArray());
    }

    private PrivStatusCode setNotifyListener(String type, String uri, VehicleStatusInformation.VehicleSysRsp.Builder rsp) {
        if(!TextUtils.isEmpty(type) && !uri.isEmpty()) {
            mService.setVehicleAttribute(type, uri);
            return PrivStatusCode.OK;

        }
        return PrivStatusCode.SRV_ACT_UNKNOWN;
    }

    private PrivStatusCode setAttribute(String type, Object val, VehicleStatusInformation.VehicleSysRsp.Builder rsp) {
        mService.setVehicleAttribute(type, val);
        return PrivStatusCode.SRV_ACT_UNKNOWN;
    }

    private void loadAttr(VehicleStatusInformation.VehicleSysRsp.Builder rsp) {
        rsp.setSystemClock(System.currentTimeMillis())
                .setOtaState(mService.getVehicleAttribute(VehicleInformation.ATTR_OTA_STATE, 0))
                .setOtaTask(mService.getVehicleAttribute(VehicleInformation.ATTR_OTA_TASK, ""))
                .setOtaProgress(mService.getVehicleAttribute(VehicleInformation.ATTR_OTA_PROGRESS, 0))
                .setOtaExtra(mService.getVehicleAttribute(VehicleInformation.ATTR_OTA_EXTRA, ""));
    }
}
