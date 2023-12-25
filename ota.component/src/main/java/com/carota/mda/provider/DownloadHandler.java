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
import com.carota.mda.download.IDownloadCtrlStatus;
import com.carota.mda.download.IDownloadSection;
import com.carota.protobuf.MasterDownloadAgent;
import com.carota.svr.HttpResp;
import com.carota.svr.PrivStatusCode;
import com.carota.svr.SimpleHandler;
import com.google.protobuf.InvalidProtocolBufferException;
import com.momock.util.Logger;

import java.util.List;
import java.util.Map;

public class DownloadHandler extends SimpleHandler {

    private UpdateMaster mMaster;

    public DownloadHandler(UpdateMaster master) {
        mMaster = master;
    }

    @Override
    public HttpResp post(String path, Map<String, List<String>> params, byte[] body, Object extra) {
        PrivStatusCode code = PrivStatusCode.SRV_ACT_UNKNOWN;
        MasterDownloadAgent.DownloadRsp.Builder builder = MasterDownloadAgent.DownloadRsp.newBuilder();
        try {
            MasterDownloadAgent.DownloadReq req = MasterDownloadAgent.DownloadReq.parseFrom(body);
            switch (req.getAction()) {
                case START:
                    code = downloadStart(builder);
                    break;
                case STOP:
                    code = downloadStop(builder);
                    break;
                case QUERY:
                    code = downloadQuery(builder);
                    break;
            }
        } catch (Exception e) {
            Logger.error(e);
        }
        return HttpResp.newInstance(code, builder.build().toByteArray());
    }

    private PrivStatusCode downloadStart(MasterDownloadAgent.DownloadRsp.Builder rsp) {
        return mMaster.startDownload() ? PrivStatusCode.OK : PrivStatusCode.SRV_ACT_UNKNOWN;
    }

    private PrivStatusCode downloadStop(MasterDownloadAgent.DownloadRsp.Builder rsp) {
        mMaster.stopDownload();
        return PrivStatusCode.OK;
    }

    private PrivStatusCode downloadQuery(MasterDownloadAgent.DownloadRsp.Builder rsp) {
        IDownloadCtrlStatus dc = mMaster.getDownloadController();
        if(dc.hasError()) {
            rsp.setStatus(MasterDownloadAgent.DownloadRsp.Status.ERROR);
        } else if (dc.isFinished()) {
            rsp.setStatus(MasterDownloadAgent.DownloadRsp.Status.READY);
        } else if (dc.isRunning()) {
            rsp.setStatus(MasterDownloadAgent.DownloadRsp.Status.RUN);
        } else {
            rsp.setStatus(MasterDownloadAgent.DownloadRsp.Status.IDLE);
        }
        IDownloadSection[] section = dc.query();
        if (null != section) {
            for (IDownloadSection s : section) {
                rsp.addTasks(MasterDownloadAgent.DownloadRsp.Task.newBuilder()
                        .setName(s.getName())
                        .setSpeed(s.getSpeed())
                        .setProgress(s.getProgress()));
            }
        }
        return PrivStatusCode.OK;
    }
}
