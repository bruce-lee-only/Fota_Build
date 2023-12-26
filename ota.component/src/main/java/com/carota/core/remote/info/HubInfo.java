/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.core.remote.info;


import android.text.TextUtils;

import com.carota.protobuf.ServiceHub;

import java.util.HashMap;
import java.util.Map;

public class HubInfo {

    private Map<String, ServiceHub.InfoRsp.Route> mRouteMap;
    private String mDesc;

    public HubInfo(ServiceHub.InfoRsp rsp) {
        mRouteMap = new HashMap<>();
        for(ServiceHub.InfoRsp.Route rt : rsp.getRoutesList()) {
            mRouteMap.put(rt.getModule(), rt);
        }
    }

    public boolean contains(String host) {
        return mRouteMap.containsKey(host);
    }

    @Override
    public String toString() {
        return TextUtils.join("; ", mRouteMap.keySet());
    }
}
