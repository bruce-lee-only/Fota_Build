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
import com.carota.svr.HttpResp;
import com.carota.svr.PrivStatusCode;
import com.carota.svr.SimpleHandler;
import com.momock.util.Logger;

import java.util.List;
import java.util.Map;

public class TestHandler extends SimpleHandler {

    private UpdateMaster mMaster;

    public TestHandler(UpdateMaster master) {
        mMaster = master;
    }

    @Override
    public HttpResp post(String path, Map<String, List<String>> params, byte[] body, Object extra) {
        PrivStatusCode code = PrivStatusCode.SRV_UNKNOWN;
        MasterDownloadAgent.TestRsp.Builder builder = MasterDownloadAgent.TestRsp.newBuilder();

        try {
            MasterDownloadAgent.TestReq req = MasterDownloadAgent.TestReq.parseFrom(body);
            switch (req.getType()) {
                case RPC:
                    code = testRpc(builder);
                    break;
                case ALIVE:
                    code = testAlive(builder);
                    break;
            }
        } catch (Exception e) {
            Logger.error(e);
        }
        return HttpResp.newInstance(code, builder.build().toByteArray());
    }

    private PrivStatusCode testRpc(MasterDownloadAgent.TestRsp.Builder rsp) {
        List<String> lostEcus = mMaster.testSystemRpc();
        rsp.addAllData(lostEcus);
        return PrivStatusCode.OK;
    }

    private PrivStatusCode testAlive(MasterDownloadAgent.TestRsp.Builder rsp) {
        //WARING: DO NOT REMOTE THIS. API for checking Master Alive
        return PrivStatusCode.OK;
    }
}
