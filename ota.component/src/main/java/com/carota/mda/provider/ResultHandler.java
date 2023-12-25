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
import com.carota.mda.data.MasterStatus;
import com.carota.mda.deploy.bean.DeployEcuResult;
import com.carota.mda.deploy.bean.DeployResult;
import com.carota.protobuf.MasterDownloadAgent;
import com.carota.svr.HttpResp;
import com.carota.svr.PrivStatusCode;
import com.carota.svr.SimpleHandler;
import com.momock.util.Logger;

import java.util.List;
import java.util.Map;

public class ResultHandler extends SimpleHandler {

    private UpdateMaster mMaster;

    public ResultHandler(UpdateMaster master) {
        mMaster = master;
    }

    @Override
    public HttpResp get(String path, Map<String, List<String>> params, Object extra) {
        Logger.debug("MDA-Console[RET] recv");
        MasterDownloadAgent.UpgradeResultRsp.Builder rsp = MasterDownloadAgent.UpgradeResultRsp.newBuilder();
        try {
            DeployResult result = mMaster.getResult();
            rsp.setUsid(result.getUsid())
                    .setStatus(getStatus(result.getmStatus()));
            for (Map.Entry<String, DeployEcuResult> next : result.getmEcuResultMap().entrySet()) {
                DeployEcuResult ecuResult = next.getValue();
                rsp.addTasks(MasterDownloadAgent.UpgradeResultRsp.Task.newBuilder()
                        .setName(ecuResult.getName())
                        .setProgress(ecuResult.getPro())
                        .setStatus(getStatus(ecuResult.getStatus())));
            }
        } catch (Exception e) {
            Logger.error(e);
        }
        return HttpResp.newInstance(PrivStatusCode.OK, rsp.build().toByteArray());
    }

    private MasterDownloadAgent.UpgradeResultRsp.Status getStatus(int status) {
        switch (status) {
            case DeployResult.UPGRADE:
                return MasterDownloadAgent.UpgradeResultRsp.Status.UPGRADE;
            case DeployResult.SUCCESS:
                return MasterDownloadAgent.UpgradeResultRsp.Status.SUCCESS;
            case DeployResult.FAILURE:
                return MasterDownloadAgent.UpgradeResultRsp.Status.FAILURE;
            case DeployResult.ERROR:
                return MasterDownloadAgent.UpgradeResultRsp.Status.ERROR;
            case DeployResult.ROLLBACK:
                return MasterDownloadAgent.UpgradeResultRsp.Status.ROLLBACK;
            default:
                return MasterDownloadAgent.UpgradeResultRsp.Status.IDLE;
        }
    }
}
