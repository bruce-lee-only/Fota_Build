/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.mda.provider;

import com.carota.mda.UpdateMaster;
import com.carota.mda.remote.info.EcuInfo;
import com.carota.mda.remote.info.VehicleDesc;
import com.carota.protobuf.MasterDownloadAgent;
import com.carota.svr.HttpResp;
import com.carota.svr.PrivStatusCode;
import com.carota.svr.SimpleHandler;
import com.momock.util.Logger;

import java.util.List;
import java.util.Map;

public class QueryHandler extends SimpleHandler {


    private UpdateMaster mMaster;

    public QueryHandler(UpdateMaster master) {
        mMaster = master;
    }

    @Override
    public HttpResp post(String path, Map<String, List<String>> params, byte[] body, Object extra) {
        PrivStatusCode code = PrivStatusCode.SRV_UNKNOWN;
        MasterDownloadAgent.QueryRsp.Builder builder = MasterDownloadAgent.QueryRsp.newBuilder();

        try {
            MasterDownloadAgent.QueryReq req = MasterDownloadAgent.QueryReq.parseFrom(body);
            switch (req.getAction()) {
                case VERSION:
                    setVersionData(builder);
                    break;
                case VEHICLE:
                    setVehicleData(builder);
                    break;
                default:
                    setVehicleData(builder);
                    setVersionData(builder);
                    break;
            }
            code = PrivStatusCode.OK;
        } catch (Exception e) {
            Logger.error(e);
        }
        return HttpResp.newInstance(code, builder.build().toByteArray());
    }

    private void setVersionData(MasterDownloadAgent.QueryRsp.Builder builder) {
        for(EcuInfo ei : mMaster.queryEcuInfo()) {
            builder.addInfo(MasterDownloadAgent.QueryRsp.EcuInfo.newBuilder()
                    .setName(ei.ID)
                    .setHardware(ei.hwVer)
                    .setSoftware(ei.swVer)
                    .setDesc(null != ei.mProps ? ei.mProps.toString() : "")
                    .build());
        }
    }


    private void setVehicleData(MasterDownloadAgent.QueryRsp.Builder builder) {
        VehicleDesc desc = mMaster.getVehicleServiceManager().queryInfo();
        builder.setModel(desc.getModel()).setVin(desc.getVin()).setBrand(desc.getBrand());
    }
}
