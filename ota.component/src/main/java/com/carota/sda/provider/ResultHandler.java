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

import android.os.RemoteException;

import com.carota.build.ParamRoute;
import com.carota.protobuf.SlaveDownloadAgent;
import com.carota.sda.SlaveState;
import com.carota.sda.UpdateSlave;
import com.carota.svr.HttpResp;
import com.carota.svr.PrivStatusCode;
import com.carota.svr.SimpleHandler;
import com.google.protobuf.InvalidProtocolBufferException;
import com.momock.util.Logger;

import java.util.List;
import java.util.Map;

public class ResultHandler extends SimpleHandler {

    private SlaveDownloadAgentService mService;

    public ResultHandler(SlaveDownloadAgentService service) {
        super();
        mService = service;
    }

    @Override
    public HttpResp post(String path, Map<String, List<String>> params, byte[] body, Object extra) {
        Logger.debug("master query ecu upgrade status");
        SlaveDownloadAgent.ResultRsp.Builder builder = SlaveDownloadAgent.ResultRsp.newBuilder();
        PrivStatusCode code;
        try {
            SlaveDownloadAgent.ResultReq req = SlaveDownloadAgent.ResultReq.parseFrom(body);
            SlaveState state = mService.agent.queryState(req.getName(0));
            SlaveDownloadAgent.ResultRsp.Task.Builder ret = SlaveDownloadAgent.ResultRsp.Task.newBuilder()
                    .setDomain(state.domain)
                    .setName(state.name)
                    .setError(state.getErrorCode())
                    .setProgress(state.getProgress());
            Logger.debug("[SlaveDownloadAgent1]->" + "name:" + state.name + "," + "state:" + state.getState());
            switch (state.getState()) {
                case SlaveState.STATE_UPGRADE:
                    ret.setStatus(SlaveDownloadAgent.ResultRsp.Status.UPGRADE);
                    break;
                case SlaveState.STATE_SUCCESS:
                    ret.setStatus(SlaveDownloadAgent.ResultRsp.Status.SUCCESS);
                    break;
                case SlaveState.STATE_ROLLBACK:
                    ret.setStatus(SlaveDownloadAgent.ResultRsp.Status.ROLLBACK);
                    break;
                case SlaveState.STATE_ERROR:
                    ret.setStatus(SlaveDownloadAgent.ResultRsp.Status.ERROR);
                    break;
                case SlaveState.STATE_FAILURE:
                    ret.setStatus(SlaveDownloadAgent.ResultRsp.Status.FAILURE);
                    break;
                default:
                    ret.setStatus(SlaveDownloadAgent.ResultRsp.Status.IDLE);
                    break;
            }
            switch (state.getMsg()) {
                case UpdateSlave.INSTALL_MSG_TRANSPORT:
                    ret.setStep(SlaveDownloadAgent.ResultRsp.Step.TRANSPORT);
                    break;
                case UpdateSlave.INSTALL_MSG_VERIFY:
                    ret.setStep(SlaveDownloadAgent.ResultRsp.Step.VERIFY);
                    break;
                case UpdateSlave.INSTALL_MSG_DEPLOY:
                    ret.setStep(SlaveDownloadAgent.ResultRsp.Step.DEPLOY);
                    break;
                case UpdateSlave.INSTALL_MSG_INTERRUPT:
                    ret.setStep(SlaveDownloadAgent.ResultRsp.Step.INTERRUPT);
                    break;
                case UpdateSlave.INSTALL_MSG_REBOOT:
                    ret.setStep(SlaveDownloadAgent.ResultRsp.Step.REBOOT);
                    break;
            }
            builder.addTasks(ret.build());
            code = PrivStatusCode.OK;
        } catch (InvalidProtocolBufferException | RemoteException e) {
            Logger.error(e);
            code = PrivStatusCode.SRV_UNKNOWN;
        }
        return HttpResp.newInstance(code, builder.build().toByteArray());
    }
}
