/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.svr;

import android.content.Intent;

public abstract class RouterService {

    protected final String mModuleName;
    protected final boolean mNotifyHub;

    public RouterService(String name, boolean notifyHub) {
        mModuleName = name;
        mNotifyHub = notifyHub;
    }

    @Override
    public String toString() {
        return mModuleName;
    }

    public boolean needNotifyHub() {
        return mNotifyHub;
    }

    public abstract void onInit(IRouterHttp server);

    public abstract void onStart(IRouterHttp server);

    public abstract void onStop(IRouterHttp server);

    public abstract void onWakeUp(IRouterHttp server, Intent i);

}
