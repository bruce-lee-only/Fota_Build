/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.config.provider;

import android.content.Context;
import android.content.Intent;

import com.carota.svr.IRouterHttp;
import com.carota.svr.RouterService;
import com.momock.util.Logger;

public class ConfigManagerService extends RouterService {

    private Context mContext;
    public ConfigManagerService(Context context, String name) {
        super(name, true);
        mContext = context;
    }

    @Override
    public void onInit(IRouterHttp server) {
        Logger.debug("CMS onInit");
        server.setRequestHandler(mModuleName, "/file", new FileHandler(mContext));
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
