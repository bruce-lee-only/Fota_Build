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

import android.content.Context;

import com.carota.mda.data.UpdateCampaign;
import com.carota.mda.UpdateMaster;
import com.carota.mda.data.MasterStatus;
import com.carota.protobuf.MasterDownloadAgent;
import com.carota.svr.HttpResp;
import com.carota.svr.PrivStatusCode;
import com.carota.svr.SimpleHandler;
import com.carota.util.ConfigHelper;
import com.momock.util.Logger;

import java.util.List;
import java.util.Map;

public class SyncHandler extends SimpleHandler {

    public static final int FLAG_DOWNLOADING = 0x00000001;
    public static final int FLAG_INSTALLING = 0x00000002;
    public static final int FLAG_DOWNLOADED = 0x00000100;
    public static final int FLAG_UPGRADE_SLAVE = 0x00000200;
    public static final int FLAG_UPGRADE_MASTER = 0x00000400;
    public static final int FLAG_UPGRADE_UI = 0x00000800;

    private final UpdateMaster mMaster;
    private final Context mContext;

    public SyncHandler(UpdateMaster master, Context context) {
        mMaster = master;
        mContext = context;
    }

    @Override
    public HttpResp get(String path, Map<String, List<String>> params, Object extra) {
        PrivStatusCode code = PrivStatusCode.SRV_UNKNOWN;
        MasterDownloadAgent.SyncRsp.Builder builder = MasterDownloadAgent.SyncRsp.newBuilder();
        try {
            MasterStatus st = mMaster.getStatus();
            builder.setUsid(st.getUSID());

            int status = 0;
            if (mMaster.isDownloading()) {
                status |= FLAG_DOWNLOADING;
            }
            if (mMaster.isInstalling()) {
                status |= FLAG_INSTALLING;
            }
            if (st.getPackage()) {
                status |= FLAG_DOWNLOADED;
            }
            if (st.getUpgradeSlave()) {
                status |= FLAG_UPGRADE_SLAVE;
            }
            if (st.getUpgradeMaster()) {
                status |= FLAG_UPGRADE_MASTER;
                status |= FLAG_UPGRADE_UI;
            }
            builder.setState(status);
            builder.setAction(ConfigHelper.isTestModeEnabled(mContext)
                    ? MasterDownloadAgent.SyncRsp.EnvmAction.UAT
                    :MasterDownloadAgent.SyncRsp.EnvmAction.PRODUCE);
            UpdateCampaign us = mMaster.getCampaign();
            if (null != us) {
                builder.addAllEnvironment(us.getCondition());
            }

            code = PrivStatusCode.OK;
        } catch (Exception e) {
            Logger.error(e);
        }
        return HttpResp.newInstance(code, builder.build().toByteArray());
    }
}
