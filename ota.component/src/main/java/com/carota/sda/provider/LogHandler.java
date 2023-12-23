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
import com.carota.svr.HttpResp;
import com.carota.svr.PrivStatusCode;
import com.carota.svr.SimpleHandler;
import com.google.protobuf.InvalidProtocolBufferException;
import com.momock.util.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LogHandler extends SimpleHandler {

    private SlaveDownloadAgentService mService;

    public LogHandler(SlaveDownloadAgentService service) {
        super();
        mService = service;
    }

    @Override
    public HttpResp post(String path, Map<String, List<String>> params, byte[] body, Object extra) {
        SlaveDownloadAgent.LogRsp.Builder rsp = SlaveDownloadAgent.LogRsp.newBuilder();
        try {
            SlaveDownloadAgent.LogReq req = SlaveDownloadAgent.LogReq.parseFrom(body);
            switch (req.getAction()) {
                case LIST:
                    return handleList(rsp, req.getSize(), req.getExtraPath());
                case DELETE:
                default:
                    return HttpResp.newInstance(PrivStatusCode.SRV_ACT_IMPLEMENTED);
            }
        } catch (InvalidProtocolBufferException e) {
            Logger.error(e);
        }
        return super.post(path, params, body, extra);
    }

    @Override
    public HttpResp get(String path, Map<String, List<String>> params, Object extra) {
        try {
            if (path.endsWith("/file")) {
                String id = params.get("id").get(0);
                return handleFile(id, extra);
            }
            return HttpResp.newInstance(PrivStatusCode.SRV_ACT_IMPLEMENTED);
        } catch (Exception e) {
            Logger.error(e);
        }
        return super.get(path, params, extra);
    }

    private HttpResp handleList(SlaveDownloadAgent.LogRsp.Builder rsp, int max, String extraPath) {
        List<String> logFiles = new ArrayList<>();
        PrivStatusCode code = PrivStatusCode.SRV_UNKNOWN;
        if (mService.agent.listLogFiles(null, max, logFiles, extraPath)) {
            code = PrivStatusCode.OK;
        }
        return HttpResp.newInstance(code, rsp.addAllFiles(logFiles).build().toByteArray());
    }

    private HttpResp handleFile(String name, Object httpContext) throws FileNotFoundException {
        File f = mService.agent.findLogFile(name);
        if(null != f) {
            return HttpResp.newRawInstance(new FileInputStream(f), f.length(), httpContext);
        } else {
            return HttpResp.newInstance(PrivStatusCode.REQ_TARGET_UNKNOWN);
        }
    }
}
