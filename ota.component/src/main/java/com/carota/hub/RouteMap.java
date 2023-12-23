/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.hub;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RouteMap {

    public static class Route {
        private String mHost;
        private String mAddr;
        private boolean mActive;

        private Route(String host, String addr) {
            mHost = host;
            mAddr = addr;
            mActive = true;
        }

        public String getHost() {
            return mHost;
        }

        public String getAddr() {
            return mAddr;
        }

        public boolean isActive() {
            return mActive;
        }
    }

    private static RouteMap sInstance = new RouteMap();

    private List<Route> mLocalRoute;
    private Map<String, Route> mRemoteRoute;

    public static RouteMap get() {
        return sInstance;
    }

    private RouteMap() {
        mLocalRoute = new ArrayList<>();
        mRemoteRoute = new HashMap<>();
    }

    public void setLocal(List<String> host) {
        // remove Duplicate item
        LinkedHashSet<String> set = new LinkedHashSet<>(host.size());
        set.addAll(host);

        host.clear();
        mLocalRoute.clear();
        for(String h : set) {
            host.add(h);
            mLocalRoute.add(new Route(h, null));
        }
    }

    public boolean addRemote(int port, String[] hosts, String ip) {
        if (null != hosts && port > 0 && !TextUtils.isEmpty(ip)) {
            String url = ip + ":" + port;
            for (int i = 0; i < hosts.length; i++) {
                if(!TextUtils.isEmpty(hosts[i])) {
                    mRemoteRoute.put(hosts[i], new Route(hosts[i], url));
                }
            }
            return true;
        }
        return false;
    }

    public Route findRemoteRoute(String host) {
        return mRemoteRoute.get(host);
    }

    public Set<Route> listRoute() {
        Set<Route> ret = new HashSet<>(mLocalRoute);
        ret.addAll(mRemoteRoute.values());
        return ret;
    }
}
