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

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import com.momock.util.Convert;
import com.momock.util.FileHelper;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ParamAnalytics extends ConfigParser {

    /**
     * <node id="analytics">
     *     <interval>3600</interval>      <!-- sync interval : sec -->
     *     <event>/v0/data</event>        <!-- Upgrade Event URL Path -->
     *     <custom>/v0/data/vin</custom>  <!-- Custom Event URL Path -->
     *     <log>/v0/data</log>            <!-- OTA LOG Upload Path -->
     </node>
     */

    private Map<String, String> mExtra;

    public ParamAnalytics() {
        super("analytics");
        mExtra = new HashMap<>();
    }

    @Override
    protected void set(String tag, String name, String val, boolean enabled) {
        if(null != tag && null != val) {
            mExtra.put(tag, val);
        }
    }

    public String getEventUrl() {
        return mockUrl(mExtra.get("event"));
    }

    public String getEventV2Url() {
        String url = mExtra.get("event_v2");
        if(url.startsWith("http")) return url;
        return mockUrl(mExtra.get("event_v2"));
    }

    public String getLogUrl() {
        return mockUrl(mExtra.get("log"));
    }

    public String getCustomUrl(){
        return mockUrl(mExtra.get("custom"));
    }

    @TargetApi(Build.VERSION_CODES.N)
    public String getEventVersion() {
        return mExtra.getOrDefault("ev", "v0");
    }

    public long getSyncInterval() {
        long defInterval = 60L;    // sec
        String val = mExtra.get("interval");
        if(null != val) {
            return Convert.toLong(val, defInterval) * 1000;
        }
        return defInterval * 1000;
    }
}
