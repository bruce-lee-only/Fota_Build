/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.vsi;

import android.content.Context;
import android.os.Bundle;

import com.carota.build.IConfiguration;
import com.carota.build.ParamRoute;
import com.carota.build.ParamVSM;
import com.carota.core.VehicleEvent;
import com.carota.mda.remote.ActionVSI;
import com.carota.mda.remote.IActionVSI;
import com.carota.mda.remote.info.IVehicleStatus;
import com.carota.core.SystemAttribute;
import com.carota.mda.remote.info.VehicleDesc;
import com.carota.util.ConfigHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VehicleServiceManager implements IActionVSI {

    private ParamVSM mParamVSM;
    private ParamRoute mParamRoute;
    private Map<String, IActionVSI> mPool;
    private IVehicleDescription mVehicleDescription;

    public VehicleServiceManager(Context context) {
        this(context, null);
    }

    public VehicleServiceManager(Context context, IVehicleDescription desc) {
        Context ctx = context.getApplicationContext();
        IConfiguration config = ConfigHelper.get(ctx);
        mParamRoute = config.get(ParamRoute.class);
        mParamVSM = config.get(ParamVSM.class);
        mPool = new HashMap<>();
        mVehicleDescription = desc;
    }

    private IActionVSI getRealVSI(String name) {
        IActionVSI act = mPool.get(name);
        if(null == act) {
            String host = mParamRoute.getRoute(name).getHost(ParamRoute.Info.PATH_VCAN);
            act = new ActionVSI(host);
            mPool.put(name, act);
        }
        return act;
    }

    @Override
    public VehicleDesc queryInfo() {
        if(null != mVehicleDescription) {
            String vin = mVehicleDescription.queryVinCode();
            String model = mVehicleDescription.queryModel();
            return new VehicleDesc(vin, model, "");
        }
        return getRealVSI(mParamVSM.getInfo()).queryInfo();
    }

    @Override
    public IVehicleStatus queryStatus() {
        return getRealVSI(mParamVSM.getCondition()).queryStatus();
    }

    @Override
    public int registerEvent(String action, String activeUri) {
        return getRealVSI(mParamVSM.getEvent()).registerEvent(action, activeUri);
    }

    @Override
    public boolean fireEvent(String action, long delaySec, Bundle extra) {
        String name = mParamVSM.getEvent();
        if (!VehicleEvent.EVENT_SCHEDULE.equals(action)) {
            name = mParamVSM.getPower();
        }
        return getRealVSI(name).fireEvent(action, delaySec, extra);
    }

    @Override
    public boolean removeEvent(String action) {
        String name = mParamVSM.getEvent();
        if (!VehicleEvent.EVENT_SCHEDULE.equals(action)) {
            name = mParamVSM.getPower();
        }
        return getRealVSI(name).removeEvent(action);
    }

    @Override
    public SystemAttribute setSystemAttribute(List<SystemAttribute.Configure> cfg) {
        return getRealVSI(mParamVSM.getEvent()).setSystemAttribute(cfg);
    }
}
