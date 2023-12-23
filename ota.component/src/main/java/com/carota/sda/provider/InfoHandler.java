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

import android.os.Bundle;

import com.carota.protobuf.SlaveDownloadAgent;
import com.carota.sda.SlaveInfo;
import com.carota.svr.HttpResp;
import com.carota.svr.PrivStatusCode;
import com.carota.svr.SimpleHandler;
import com.google.protobuf.InvalidProtocolBufferException;
import com.momock.util.Logger;

import java.util.List;
import java.util.Map;

public class InfoHandler extends SimpleHandler {

    private SlaveDownloadAgentService mService;

    public InfoHandler(SlaveDownloadAgentService service) {
        super();
        mService = service;
    }

    @Override
    public HttpResp post(String path, Map<String, List<String>> params, byte[] body, Object extra) {
        SlaveDownloadAgent.InfoRsp.Builder builder = SlaveDownloadAgent.InfoRsp.newBuilder();
        try {
            SlaveDownloadAgent.InfoReq rsp = SlaveDownloadAgent.InfoReq.parseFrom(body);
            Logger.error("@InfoHandler name : " + rsp.getName());
            Bundle bundle = new Bundle();
            bundle.putByteArray("bom", body);
            SlaveInfo si = mService.agent.readInfo(rsp.getName(), bundle);
            builder.setHardware(si.getProp(SlaveInfo.PROP_VER_HW))
                    .setSoftware(si.getProp(SlaveInfo.PROP_VER_SW))
                    .setDa(si.getProp(SlaveInfo.PROP_VER_DA))
                    .setSn(si.getProp(SlaveInfo.PROP_SN))
                    .setExtra(si.subJsonString());
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        return HttpResp.newInstance(PrivStatusCode.OK, builder.build().toByteArray());
    }
}
