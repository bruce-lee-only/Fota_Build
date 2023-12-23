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

import com.momock.util.Convert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParamRMDA extends ModuleParser {

    /**
     *  <node id="rmda">
     *         <name>rescue_master</name>
     *         <conn>/pivot/ota/v0/vconnect</conn>
     *         <check>/pivot/ota/v0/check/schedule</check>
     *         <dm name="dm_ivi" />
     *         <dm name="dm_tbox">tbox,vcu</dm>
     *         <timeout>600</timeout>
     *         <vsi>vsi_tbox</vsi>
     *         <secure enabled="false"/>
     *         <secureretry></secureretry>
     *     </node>
     */

    private long INSTALL_TIMEOUT = 30 * 60;
    private Map<String, String> mExtra;
    private List<String> mDownloadManagerPool;
    private long mTimeout;
    private String mSecureOption;
    private int mSecureCacheRetry;

    public ParamRMDA() {
        super("rmda");
        mExtra = new HashMap<>();
        mTimeout = INSTALL_TIMEOUT * 1000;
        mDownloadManagerPool = new ArrayList<>();
        mSecureOption = null;
    }

    @Override
    protected void setExtras(String tag, String name, String val, boolean enabled) {
        switch (tag) {
            case "timeout":
                mTimeout = Convert.toLong(val, INSTALL_TIMEOUT) * 1000;
                break;
            case "dm":
                // DM DATA = [Head Length as char][DM Name],[val]
                // eg : 3dm,tbox,vcu,ivi
                String data = (char) (name.length() + 1) + name + ',' + val;
                if (TextUtils.isEmpty(val)) {
                    mDownloadManagerPool.add(0, data);
                } else {
                    mDownloadManagerPool.add(data);
                }
                break;
            case "secure":
                // PKI is enabled once secure tag is set, for attr enabled
                //    if true, only ecus in the list enabled PKI.
                //    if false, only ecus in the list disable PKI.
                // format as "true,xx,yy,zz"
                // search key ",xx" at guard pool
                if (null != val) {
                    mSecureOption = enabled + "," + val.trim();
                } else {
                    mSecureOption = enabled + ",";
                }
                break;
            case "retry":
                mSecureCacheRetry = Convert.toInteger(val, 0);
                break;
            default:
                mExtra.put(tag, val);
                break;
        }
    }

    public String getFakeVin(String sn) {
        StringBuilder sb = new StringBuilder(64);
        sb.append(mExtra.get("model"));
        if (sb.length() != 17) {
            sb.append(sn);
        }
        return sb.toString();
    }

    public String getBomUrl() {
        return mockUrl(mExtra.get("bom"));
    }

    public String getConnUrl() {
        return mockUrl(mExtra.get("conn"));
    }

    public String getCheckUrl() {
        return mockUrl(mExtra.get("check"));
    }

    public long getTimeout() {
        return mTimeout;
    }

    public String findDownloadManagerName(String ecu) {
        if(!TextUtils.isEmpty(ecu)) {
            for (String mgr : mDownloadManagerPool) {
                int drift = mgr.charAt(0);
                if (mgr.indexOf(ecu, drift) >= 0) {
                    return mgr.substring(1, drift);
                }
            }
        }
        String defMgr = mDownloadManagerPool.get(0);
        return defMgr.substring(1, defMgr.charAt(0));
    }

    public String getDownloadManagerName() {
        return findDownloadManagerName(null);
    }

    public String[] listDownloadManager() {
        List<String> ret = new ArrayList<>();
        for (String mgr : mDownloadManagerPool) {
            int drift = mgr.charAt(0);
                ret.add(mgr.substring(1, drift));
        }
        return ret.toArray(new String[0]);
    }

    public String getVeihcelStatusInfoName() {
        return mExtra.get("vsi");
    }

    public boolean isSecureEnabled(String ecu) {
        if(null == mSecureOption) {
            // disabled
            return false;
        } else if(mSecureOption.startsWith("true")) {
            // dual as whitelist
            return mSecureOption.contains("," + ecu);
        } else {
            // dual as blacklist
            return !mSecureOption.contains("," + ecu);
        }
    }

    public int getSecureCacheRetry() {
        return mSecureCacheRetry;
    }
}
