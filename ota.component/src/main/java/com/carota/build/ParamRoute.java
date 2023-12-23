/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.build;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ParamRoute extends ConfigParser {

    /**
     * <node id="route">
     *    <can name="dvr">ota_sub</can>
     *    <eth name="dvr">ota_ivi</eth>
     *
     *    <veth name="dm">ota_dm</eth>
     *    <vcan name="vsi">ota_vsi</can>
     * </node>
     */

    public static class Info {
        private static final int MAX_INFO_COUNT = 4;
        public static final int PATH_ETH = 0x00000001;
        public static final int PATH_CAN = 0x00000002;
        public static final int PATH_VETH = 0x00000004;
        public static final int PATH_VCAN = 0x00000008;

        public final String ID;
        private int mPathDesc;

        /**
         * mHost[0]: host to eth
         * mHost[1]: host to can
         * mHost[2]: host to veth(virtual eth)
         * mHost[3]: host to vcan(virtual can)
         */
        private String[] mHost;

        private Info(String id) {
            ID = id;
            mPathDesc = 0;
            mHost = new String[MAX_INFO_COUNT];
        }

        public String getHost(int path) {
            int index = findIndex(path);
            if(index >= 0 && index < mHost.length) {
                return mHost[index];
            }
            return null;
        }

        private Info setHost(int path, String host) {
            mHost[findIndex(path)] = host;
            mPathDesc |= path;
            return this;
        }

        public int getPathDesc() {
            return mPathDesc;
        }

        private int findIndex(int pathFlag) {
            if(pathFlag >= 1) {
                for (int i = 0; i < MAX_INFO_COUNT; i++) {
                    if (pathFlag == (1 << i)) {
                        return i;
                    }
                }
            }
            throw new RuntimeException("Can Not Find Index From Path: ParamRoute.Info");
        }
    }

    private Map<String, Info> mRouteMap;
    private final static String SUB_NAME = "sub";

    public ParamRoute() {
        super("route");
        // WARNING: Must use ordered map
        mRouteMap = new LinkedHashMap<>();
    }

    @Override
    protected void set(String tag, String name, String val, boolean enabled) {
        if(TextUtils.isEmpty(val)) {
            return;
        }

        Info info = mRouteMap.get(name);
        if(null == info) {
            info = new Info(name);
        }
        int type;
        switch (tag) {
            case "eth":
                type = Info.PATH_ETH;
                break;
            case "can":
                type = Info.PATH_CAN;
                break;
            case "veth":
                type = Info.PATH_VETH;
                break;
            case "vcan":
                type = Info.PATH_VCAN;
                break;
            default:
                return;
        }
        mRouteMap.put(name, info.setHost(type, val));
    }

    public Info getRoute(String name) {
        return mRouteMap.get(name);
    }

    public List<Info> listInfo(int tester) {
        List<Info> ecus = new ArrayList<>();
        for(Info info : mRouteMap.values()) {
            if((info.mPathDesc & tester) > 0) {
                ecus.add(info);
            }
        }
        return ecus;
    }

    public List<Info> listEcuInfo() {
        return listInfo(Info.PATH_ETH);
    }

    public static String getEcuHost(Info info) {
        if (info != null) {
            String host = info.mHost[0];
            if(!TextUtils.isEmpty(host)) {
                return host;
            }
            host = info.mHost[1];
            if(!TextUtils.isEmpty(host)) {
                return host;
            }
        }
        return null;
    }

    public String getSubHost() {
        Info info = mRouteMap.get(SUB_NAME);
        if (info==null) return null;
        return getEcuHost(info);
    }
}
