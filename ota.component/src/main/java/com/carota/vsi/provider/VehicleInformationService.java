/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.vsi.provider;

import android.content.Context;
import android.content.Intent;

import com.carota.svr.IRouterHttp;
import com.carota.svr.RouterService;
import com.carota.vsi.VehicleInformation;

public class VehicleInformationService extends RouterService {

    private VehicleInformation mVSI;

    public VehicleInformationService(Context context, String host) {
        super(host, true);
        mVSI = new VehicleInformation(context);
    }

    @Override
    public void onInit(IRouterHttp server) {
        server.setRequestHandler(mModuleName, "/info", new VehicleDataHandler(mVSI));
        server.setRequestHandler(mModuleName, "/cdt", new VehicleConditionHandler(mVSI));
        server.setRequestHandler(mModuleName, "/event", new VehicleEventHandler(mVSI));
        server.setRequestHandler(mModuleName, "/sys", new VehicleSystemHandler(mVSI));
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
