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
import android.content.Intent;

import com.carota.mda.UpdateMaster;
import com.carota.mda.deploy.IDeploySafety;
import com.carota.mda.security.ISecuritySolution;
import com.carota.svr.IRouterHttp;
import com.carota.svr.RouterService;
import com.carota.vsi.IVehicleDescription;

public class MasterDownloadAgentService extends RouterService {

    public static final String HUB_BEAT_PATH = "/cmh_beat";
    protected Context mContext;
    private UpdateMaster mMaster;

    public MasterDownloadAgentService(Context context, String name, ISecuritySolution solution,
                                      IDeploySafety safety, IVehicleDescription description) {
        super(name, true);
        mContext = context;
        mMaster = new UpdateMaster(mContext, solution, safety, description);
    }

    @Override
    public void onInit(IRouterHttp server) {
        UpdateMaster master = mMaster.init();
        server.setRequestHandler(mModuleName, "/connect", new CheckHandler(master));
        server.setRequestHandler(mModuleName, "/download", new DownloadHandler(master));
        server.setRequestHandler(mModuleName, "/upgrade", new UpgradeHandler(master));
        server.setRequestHandler(mModuleName, "/result", new ResultHandler(master));
        server.setRequestHandler(mModuleName, "/test", new TestHandler(master));
        server.setRequestHandler(mModuleName, "/sync", new SyncHandler(master, mContext));
        server.setRequestHandler(mModuleName, "/query", new QueryHandler(master));
        server.setRequestHandler(mModuleName, "/event", new EventHandler(master));
        server.setRequestHandler(mModuleName, "/environment", new EnvmHandler(master,mContext));

        server.setRequestHandler(mModuleName, HUB_BEAT_PATH, new NotifyCenterHandler());
    }

    @Override
    public void onStart(IRouterHttp server) {

    }

    @Override
    public void onStop(IRouterHttp server) {

    }

    @Override
    public void onWakeUp(IRouterHttp server, Intent i) {

    }
}
