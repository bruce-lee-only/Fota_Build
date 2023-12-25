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
import com.carota.protobuf.MasterDownloadAgent;
import com.carota.protobuf.Telemetry;
import com.carota.svr.HttpResp;
import com.carota.svr.PrivStatusCode;
import com.carota.svr.SimpleHandler;
import com.momock.util.Logger;

import java.util.List;
import java.util.Map;

public class UpgradeHandler extends SimpleHandler {

    private final UpdateMaster mMaster;

    public UpgradeHandler(UpdateMaster master) {
        mMaster = master;
    }

    @Override
    public HttpResp post(String path, Map<String, List<String>> params, byte[] body, Object extra) {
        PrivStatusCode code = PrivStatusCode.SRV_ACT_UNKNOWN;
        Telemetry.EmptyRsp.Builder builder = Telemetry.EmptyRsp.newBuilder();
        try {
            MasterDownloadAgent.UpgradeReq req = MasterDownloadAgent.UpgradeReq.parseFrom(body);
            String usid = req.getUsid();
            if (mMaster.triggerMasterUpgrade(usid)) {
                code = PrivStatusCode.OK;
            }
        } catch (Exception e) {
            Logger.error(e);
        }
        return HttpResp.newInstance(code, builder.build().toByteArray());
    }
}
