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
import com.momock.util.Logger;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class EventHandler extends SimpleHandler {

    private SlaveDownloadAgentService mService;

    public EventHandler(SlaveDownloadAgentService service) {
        super();
        mService = service;
    }

    @Override
    public HttpResp post(String path, Map<String, List<String>> params, byte[] body, Object extra) {
        SlaveDownloadAgent.EventRsp.Builder builder = SlaveDownloadAgent.EventRsp.newBuilder();
        PrivStatusCode code = PrivStatusCode.SRV_UNKNOWN;
        try {
            SlaveDownloadAgent.EventReq evtReq = SlaveDownloadAgent.EventReq.parseFrom(body);
            switch (evtReq.getAction()) {
                case DELETE:
                    if(mService.agent.deleteEvent(evtReq.getIdsList())) {
                        code = PrivStatusCode.OK;
                    } else {
                        code = PrivStatusCode.SRV_ACT_UNKNOWN;
                    }
                    break;
                case FETCH:
                    List<String> logs = new LinkedList<>();
                    List<String> ids = new ArrayList<>();
                    if (mService.agent.fetchEvent(evtReq.getType(), evtReq.getSize(), logs, ids)) {
                        code = PrivStatusCode.OK;
                        for(int i = 0; i < ids.size(); i++) {
                            builder.addEvents(SlaveDownloadAgent.EventRsp.Event.newBuilder()
                                    .setId(ids.get(i)).setData(logs.get(i)));
                        }
                        return HttpResp.newInstance(code, builder.build().toByteArray());
                    }
                    break;
            }
            return HttpResp.newInstance(code, builder.build().toByteArray());
        } catch (Exception e) {
            Logger.error(e);
        }
        return super.post(path, params, body, extra);
    }
}
