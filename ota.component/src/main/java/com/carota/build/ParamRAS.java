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

import com.momock.util.Convert;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ParamRAS extends ConfigParser {

    /**
     * <node id="sda" type="dvr">
     *     <name>ota_sda</name>
     *     <agent>ota_dvr<agent/>          <!-- [OPT] -->
     *     <pkg>com.carota.agent</pkg>     <!-- [OPT] -->
     *     <timeout>1000</timeout>         <!-- 1000sec -->
     *     <guard>enable</guard>           <!-- [OPT] -->
     *     <retry>3</retry>                <!-- [OPT] -->
     * </node>
     */

    public static class Info {
        private String mId;
        private String mHost;
        private String mPkg;
        private long mTimeout;
        private String mAgent;
        private boolean mGuardEnable;
        private int mRetry;

        private Info(String id) {
            mId = id;
            mHost = null;
            mPkg = null;
            mGuardEnable = false;
            mRetry = 1;
        }

        public String getId() {
            return mId;
        }

        public String getHost() {
            return mHost;
        }

        public String getPackage() {
            return mPkg;
        }

        public long getResumeTimeout() {
            return mTimeout;
        }

        public String getAgent(){
            return mAgent ==null ? mId : mAgent;
        }

        public boolean isGuardEnable() {
            return mGuardEnable;
        }

        public int getMaxRetry() {
            return mRetry;
        }
    }

    private Map<String, Info> mExtra;
    private Info mInfoTemp;

    public ParamRAS() {
        super("ras");
        mExtra = new HashMap<>();
    }

    @Override
    protected void setType(String type) {
        if(null != type) {
            mInfoTemp = mExtra.get(type);
            if(null == mInfoTemp) {
                mInfoTemp = new Info(type);
                mExtra.put(type, mInfoTemp);
            }
        }
    }

    @Override
    protected void set(String tag, String name, String val, boolean enabled) {
        if(null != mInfoTemp && null == name) {
            switch (tag) {
                case "name":
                    mInfoTemp.mHost = val;
                    break;
                case "pkg":
                    mInfoTemp.mPkg = val;
                    break;
                case "timeout":
                    mInfoTemp.mTimeout = Convert.toLong(val, 0L) * 1000;
                    break;
                case "agent":
                    mInfoTemp.mAgent = val;
                    break;
                case "guard":
                    mInfoTemp.mGuardEnable = val.equalsIgnoreCase("enable");
                    break;
                case "retry":
                    mInfoTemp.mRetry = Convert.toInteger(val, 1);
                    break;

            }
        }
    }

    public Collection<Info> listInfo() {
        return mExtra.values();
    }

    public Info findInfoByType(String type) {
        return mExtra.get(type);
    }

}
