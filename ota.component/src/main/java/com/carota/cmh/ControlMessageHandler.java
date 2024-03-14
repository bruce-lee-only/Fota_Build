/*******************************************************************************
 * Copyright (C) 2018-2021 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/
package com.carota.cmh;


import android.content.Context;

import com.carota.build.IConfiguration;
import com.carota.build.ParamCMH;
import com.carota.build.ParamRoute;
import com.carota.core.ScheduleAttribute;
import com.carota.mda.remote.ActionCMH;
import com.carota.mda.remote.IActionCMH;
import com.carota.util.ConfigHelper;

import java.util.HashMap;
import java.util.Map;

public class ControlMessageHandler implements IActionCMH {
    private ParamCMH mParamCMH;
    private ParamRoute mParamRoute;
    private Map<String, IActionCMH> mPool;

    public ControlMessageHandler(Context context) {
        Context ctx = context.getApplicationContext();
        IConfiguration config = ConfigHelper.get(ctx);
        mParamRoute = config.get(ParamRoute.class);
        mParamCMH = config.get(ParamCMH.class);
        mPool = new HashMap<>();
    }

    private IActionCMH getRealCMH(String name) {
        IActionCMH cmh = mPool.get(name);
        if (cmh == null) {
            String host = mParamRoute.getRoute(name).getHost(ParamRoute.Info.PATH_VCAN);
            cmh = new ActionCMH(host);
        }
        return cmh;
    }

    @Override
    public boolean setScheduleField(String tag,long time, int type, String tid, String track) {
        return getRealCMH(mParamCMH.getHost()).setScheduleField(tag,time, type, tid, track);
    }

    @Override
    public ScheduleAttribute getScheduleField(String tag) {
        return getRealCMH(mParamCMH.getHost()).getScheduleField(tag);
    }

    @Override
    public boolean setErrorWakeUpField(String tag, int code, String desc, String track) {
        return getRealCMH(mParamCMH.getHost()).setErrorWakeUpField(tag, code, desc, track);
    }
}
