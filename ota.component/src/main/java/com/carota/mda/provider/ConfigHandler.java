package com.carota.mda.provider;

import com.carota.config.ConfigManager;
import com.carota.config.data.ConfigInfo;
import com.carota.mda.UpdateMaster;
import com.carota.protobuf.MasterDownloadAgent;
import com.carota.svr.HttpResp;
import com.carota.svr.PrivStatusCode;
import com.carota.svr.SimpleHandler;
import com.momock.util.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/*******************************************************************************
 * Copyright (C) 2022-2025 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/
public class ConfigHandler extends SimpleHandler {
    private UpdateMaster mMaster;

    public ConfigHandler(UpdateMaster master) {
        mMaster = master;
    }

    public HttpResp post(String path, Map<String, List<String>> params, byte[] body, Object extra) {
        PrivStatusCode code = PrivStatusCode.SRV_ACT_UNKNOWN;
        MasterDownloadAgent.ConfigRsp.Builder builder = MasterDownloadAgent.ConfigRsp.newBuilder();
        try {
            MasterDownloadAgent.ConfigReq req = MasterDownloadAgent.ConfigReq.parseFrom(body);
            List<String> names = new ArrayList<>();
            for (String name : req.getConfigNamesList()) {
                names.add(name);
            }
            code = obtainConfigSets(builder, names);
        } catch (Exception e) {
            Logger.error(e);
        }
        return HttpResp.newInstance(code, builder.build().toByteArray());
    }

    private PrivStatusCode obtainConfigSets(MasterDownloadAgent.ConfigRsp.Builder rsp, List<String> names) {
        List<ConfigInfo> list = ConfigManager.obtainConfigs(names);
        if (list == null) {
            return PrivStatusCode.SRV_UNKNOWN;
        } else {
            for (ConfigInfo info: list) {
                MasterDownloadAgent.ConfigRsp.ConfigUrl.Builder data = MasterDownloadAgent.ConfigRsp.ConfigUrl.newBuilder();
                data.setConfigName(info.getMd5());
                data.setUrl(info.getFileUrl());
                rsp.addConfigUrls(data);
            }
            return PrivStatusCode.OK;
        }
    }
}
