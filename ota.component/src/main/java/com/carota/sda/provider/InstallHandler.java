/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.sda.provider;

import com.carota.protobuf.SlaveDownloadAgent;
import com.carota.protobuf.Telemetry;
import com.carota.sda.SlaveTask;
import com.carota.svr.HttpResp;
import com.carota.svr.PrivStatusCode;
import com.carota.svr.SimpleHandler;
import com.momock.util.Logger;

import org.json.JSONObject;

import java.util.List;
import java.util.Map;

public class InstallHandler extends SimpleHandler {

    private SlaveDownloadAgentService mService;

    public InstallHandler(SlaveDownloadAgentService service) {
        super();
        mService = service;
    }

    @Override
    public HttpResp post(String path, Map<String, List<String>> params, byte[] body, Object extra) {
        PrivStatusCode code = PrivStatusCode.SRV_ACT_UNKNOWN;
        Telemetry.EmptyRsp.Builder builder = Telemetry.EmptyRsp.newBuilder();
        try {
            if (mService.agent.isRunning()) {
                code = PrivStatusCode.REQ_SEQ_TRIGGER;
            } else {
                SlaveTask info = SlaveTask.parseFrom(SlaveDownloadAgent.InstallReq.parseFrom(body));
                if (mService.agent.triggerUpgrade(info)) {
                    return HttpResp.newInstance(PrivStatusCode.OK, builder.build().toByteArray());
                }
            }
        } catch (Exception e) {
            Logger.error(e);
        }
        return HttpResp.newInstance(code, builder.build().toByteArray());
    }

}
