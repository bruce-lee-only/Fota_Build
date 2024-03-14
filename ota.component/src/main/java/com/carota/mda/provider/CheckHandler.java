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

import com.carota.core.ISession;
import com.carota.core.data.UpdateSession;
import com.carota.mda.data.UpdateCampaign;
import com.carota.mda.UpdateMaster;
import com.carota.mda.data.UpdateItem;
import com.carota.protobuf.MasterDownloadAgent;
import com.carota.svr.HttpResp;
import com.carota.svr.PrivStatusCode;
import com.carota.svr.SimpleHandler;
import com.momock.util.Logger;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class CheckHandler extends SimpleHandler {

    private UpdateMaster mMaster;

    public CheckHandler(UpdateMaster master) {
        mMaster = master;
    }

    @Override
    public HttpResp post(String path, Map<String, List<String>> params, byte[] body, Object extra) {
        PrivStatusCode code = PrivStatusCode.SRV_ACT_UNKNOWN;
        MasterDownloadAgent.ConnRsp.Builder builder = MasterDownloadAgent.ConnRsp.newBuilder();
        try {
            MasterDownloadAgent.ConnReq req = MasterDownloadAgent.ConnReq.parseFrom(body);
            switch (req.getAction()) {
                case SYNC:
                    code = syncData(builder, false);
                    break;
                case CHECK:
                    code = connToServer(builder,req.getLang(),false);
                    break;
                case FACTORY:
                    code = connToServer(builder,req.getLang(),true);
                    break;
                case VERIFY:
                    code = syncData(builder, true);
                    break;
            }
        } catch (Exception e) {
            Logger.error(e);
        }
        return HttpResp.newInstance(code, builder.build().toByteArray());
    }

    private PrivStatusCode syncData(MasterDownloadAgent.ConnRsp.Builder rsp, boolean verify) {
        AtomicBoolean serverIsAvailable = new AtomicBoolean(true);
        UpdateCampaign us = mMaster.SynchronousData(verify, serverIsAvailable);
        if(null != us) {
            setSession(us, rsp);
            return serverIsAvailable.get() ? PrivStatusCode.OK : PrivStatusCode.SRV_ACT_REMOTE;
        }
        return PrivStatusCode.SRV_UNKNOWN;
    }

    private PrivStatusCode connToServer(MasterDownloadAgent.ConnRsp.Builder rsp,String lang,boolean isFactory) {
        if (!mMaster.reset()) {
            return PrivStatusCode.REQ_SEQ_TRIGGER;
        }
        UpdateCampaign us = mMaster.doConnectToServer(lang,isFactory);
        if(null != us) {
            setSession(us, rsp);
            return PrivStatusCode.OK;
        } else {
            return PrivStatusCode.SRV_UNKNOWN;
        }
    }

    private void setSession(UpdateCampaign us, MasterDownloadAgent.ConnRsp.Builder rsp) {
        rsp.setUsid(us.getUSID())
                .setVin(us.getVinCode())
                .setRn(us.getReleaseNote())
                .setCampaignId(us.getCampaignId())
                .setScheduleId(us.getScheduleId())
                .addAllOperation(us.getOperation())
                .setDisplayInfoUrl(us.getDisplayInfoUrl())
                .setUpdateTime(us.getUpdateTime())
                .addAllEnvironment(us.getCondition());

        switch (us.getMode()) {
            case ISession.MODE_AUTO_DOWNLOAD:
                rsp.setMode(MasterDownloadAgent.ConnRsp.Mode.AUTO_DOWNLOAD);
                break;
            case ISession.MODE_AUTO_INSTALL:
                rsp.setMode(MasterDownloadAgent.ConnRsp.Mode.AUTO_INSTALL);
                break;
            case ISession.MODE_USER_CONFIRM:
                rsp.setMode(MasterDownloadAgent.ConnRsp.Mode.BY_USER);
                break;
            case ISession.MODE_USER_LIMIT:
                rsp.setMode(MasterDownloadAgent.ConnRsp.Mode.BY_USER_LIMIT);
                break;
            case ISession.MODE_AUTO_INSTALL_SCHEDULE:
                rsp.setMode(MasterDownloadAgent.ConnRsp.Mode.AUTO_INSTALL_SCHEDULE);
				break;
            case ISession.MODE_AUTO_UPDATE_FACTORY:
                rsp.setMode(MasterDownloadAgent.ConnRsp.Mode.AUTO_UPDATE_FACTORY);
                break;
        }

        for(int i = 0; i < us.getItemCount(); i++) {
            UpdateItem task = us.getItem(i);
            rsp.addInfo(MasterDownloadAgent.ConnRsp.UpgradeInfo.newBuilder()
                    .setSize(task.getProp(UpdateItem.PROP_DST_SIZE, 0L) + task.getProp(UpdateItem.PROP_SRC_SIZE, 0L))
                    .setName(task.getProp(UpdateItem.PROP_NAME))
                    .setDstVer(task.getProp(UpdateItem.PROP_DST_VER))
                    .setSrcVer(task.getProp(UpdateItem.PROP_SRC_VER))
                    .setRn(task.getProp(UpdateItem.PROP_RELEASE_NOTE))
                    .setTime(task.getProp(UpdateItem.PROP_UPDATE_TIME,0L))
            );
        }
    }
}
