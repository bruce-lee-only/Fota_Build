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

    private UpdateMaster mMaster;

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
            switch (req.getStep()) {
                case SLAVE:
                    code = triggerSlave(usid, builder);
                    break;
                case MASTER:
                    code = triggerMaster(usid, builder);
                    break;
                case UI:
                    code = triggerUI(usid, builder);
                    break;
            }
        } catch (Exception e) {
            Logger.error(e);
        }
        return HttpResp.newInstance(code, builder.build().toByteArray());
    }

    private PrivStatusCode triggerSlave(String usid, Telemetry.EmptyRsp.Builder rsp) {
        if(mMaster.triggerSlaveUpgrade(usid)) {
            return PrivStatusCode.OK;
        } else {
            return PrivStatusCode.SRV_ACT_UNKNOWN;
        }
    }

    private PrivStatusCode triggerMaster(String usid, Telemetry.EmptyRsp.Builder rsp) {
        int ret = mMaster.triggerMasterUpgrade(usid);
        if(ret > 0) {
            return PrivStatusCode.OK;
        } else if(0 == ret){
            return PrivStatusCode.REQ_SEQ_TRIGGER;
        } else {
            return PrivStatusCode.SRV_ACT_UNKNOWN;
        }
    }

    private PrivStatusCode triggerUI(String usid, Telemetry.EmptyRsp.Builder rsp) {
        return PrivStatusCode.OK;
    }
}
