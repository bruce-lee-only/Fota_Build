/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.sda.util;

import android.content.Context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RemoteAgentServiceManager {

    private final static RemoteAgentServiceManager sRasMgr = new RemoteAgentServiceManager();
    private final Map<String, RemoteAgentServiceHolder> mHolderPool;
    private RemoteAgentCallback mRac;

    public static RemoteAgentServiceManager get() {
        return sRasMgr;
    }

    private RemoteAgentServiceManager() {
        mHolderPool = new HashMap<>();
        mRac = null;
    }

    private synchronized RemoteAgentCallback getCallback(Context context) {
        if(null == mRac) {
            mRac = new RemoteAgentCallback(context);
        }
        return mRac;
    }

    public synchronized boolean add(String pkgName, String action, RemoteAgentCallback rac) {
        if(!mHolderPool.containsKey(pkgName)) {
            mHolderPool.put(pkgName,
                    new RemoteAgentServiceHolder(pkgName, action, rac));
            return true;
        }
        return false;
    }

    public synchronized boolean add(Context context, String pkgName, String action) {
        return add(pkgName, action, getCallback(context));
    }

//    public boolean addAll(Context context, List<String> pkgNameList) {
//        boolean ret = true;
//        RemoteAgentCallback rac = getCallback(context);
//        for(String name : pkgNameList) {
//            ret = ret && add(name, rac);
//        }
//        return ret;
//    }

    public RemoteAgentServiceHolder findAgentHolder(String pkg) {
        for(RemoteAgentServiceHolder rash : mHolderPool.values()) {
            if(pkg.equals(rash.toString())) {
                return rash;
            }
        }
        return null;
    }

}
