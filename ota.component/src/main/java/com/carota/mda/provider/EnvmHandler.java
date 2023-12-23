package com.carota.mda.provider;

import android.content.Context;

import com.carota.mda.UpdateMaster;
import com.carota.protobuf.MasterDownloadAgent;
import com.carota.svr.HttpResp;
import com.carota.svr.PrivStatusCode;
import com.carota.svr.SimpleHandler;
import com.carota.util.ConfigHelper;
import com.momock.util.Logger;

import java.util.List;
import java.util.Map;

public class EnvmHandler extends SimpleHandler {
    private final UpdateMaster mMaster;
    private final Context mContext;

    public EnvmHandler(UpdateMaster master, Context context) {
        mMaster = master;
        mContext = context;
    }

    @Override
    public HttpResp post(String path, Map<String, List<String>> params, byte[] body, Object extra) {
        PrivStatusCode code = PrivStatusCode.SRV_ACT_UNKNOWN;
        try {
            MasterDownloadAgent.EnvmReq req = MasterDownloadAgent.EnvmReq.parseFrom(body);
            ConfigHelper.setTestModeEnabled(mContext, req.getAction() == MasterDownloadAgent.EnvmReq.EnvmAction.UAT);
        } catch (Exception e) {
            Logger.error(e);
        }
        return HttpResp.newInstance(code);
    }
}
