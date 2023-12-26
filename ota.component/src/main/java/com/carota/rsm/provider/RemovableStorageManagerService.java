/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.rsm.provider;

import android.content.Intent;

import com.carota.svr.IRouterHttp;
import com.carota.svr.RouterService;
import com.momock.util.Logger;

public class RemovableStorageManagerService extends RouterService{


    public RemovableStorageManagerService(String name) {
        super(name, true);
    }

    @Override
    public void onInit(IRouterHttp server) {
        Logger.debug("RSMS onInit");
        server.setRequestHandler(mModuleName, "/file", new FileHandler());
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
