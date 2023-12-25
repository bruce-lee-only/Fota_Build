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

import com.carota.protobuf.VehicleStatusInformation;
import com.carota.svr.HttpResp;
import com.carota.svr.PrivStatusCode;
import com.carota.svr.SimpleHandler;
import com.carota.vsi.VehicleInformation;
import com.carota.vsi.data.Description;
import com.momock.util.Logger;

import java.util.List;
import java.util.Map;

public class VehicleDataHandler extends SimpleHandler {

    private VehicleInformation mService;

    public VehicleDataHandler(VehicleInformation service) {
        super();
        mService = service;
    }

    @Override
    public HttpResp post(String path, Map<String, List<String>> params, byte[] body, Object extra) {
        PrivStatusCode code = PrivStatusCode.SRV_UNKNOWN;
        VehicleStatusInformation.VehicleInfoRsp.Builder builder = VehicleStatusInformation.VehicleInfoRsp.newBuilder();

        try {
            code = queryInfo(builder);
        } catch (Exception e) {
            Logger.error(e);
        }
        return HttpResp.newInstance(code, builder.build().toByteArray());
    }

    private PrivStatusCode queryInfo(VehicleStatusInformation.VehicleInfoRsp.Builder rsp) throws Exception {
        Description desc = mService.readDescriptionFromFile();
        if(null == desc) {
            desc = mService.queryDescription();
        }
        if(null != desc) {
            rsp.setVin(desc.getVinCode())
                    .setModel(desc.getModel())
                    .setBrand(desc.getBrand());
            return PrivStatusCode.OK;
        } else {
            return PrivStatusCode.SRV_UNKNOWN;
        }
    }
}
