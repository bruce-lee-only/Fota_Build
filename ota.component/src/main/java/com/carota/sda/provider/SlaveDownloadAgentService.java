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

import android.content.Context;
import android.content.Intent;

import com.carota.build.ParamDM;
import com.carota.sda.ISlaveDownloadAgent;
import com.carota.svr.IRouterHttp;
import com.carota.svr.RouterService;
import com.carota.util.ConfigHelper;

public class SlaveDownloadAgentService extends RouterService {

    public final Context context;
    public final ISlaveDownloadAgent agent;

    public SlaveDownloadAgentService(Context context, ISlaveDownloadAgent agent) {
        super(agent.getHost(), true);
        this.context = context;
        this.agent = agent;
    }

    @Override
    public void onInit(IRouterHttp server) {
        ParamDM paramDM = ConfigHelper.get(context).get(ParamDM.class);
        agent.init(context, paramDM.getDownloadDir(context));
        server.setRequestHandler(mModuleName, "/info", new InfoHandler(this));
        server.setRequestHandler(mModuleName, "/install", new InstallHandler(this));
        server.setRequestHandler(mModuleName, "/result", new ResultHandler(this));
        server.setRequestHandler(mModuleName, "/event", new EventHandler(this));
        LogHandler logHandler = new LogHandler(this);
        server.setRequestHandler(mModuleName, "/log", logHandler);
        server.setRequestHandler(mModuleName, "/log/*", logHandler);
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
