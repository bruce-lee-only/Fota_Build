/*******************************************************************************
 * Copyright (C) 2018-2023 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.build;
//<!--If you do not need any proxy from other device,please make enabled false.
//        This proxy is usually not mixed with net_proxy.-->
//<node
//    enabled="true"
//    id="external_proxy">
//    <name>ota_net_proxy</name>
//    <!--If you want use external proxy all the time,please make whitelist enabled false.-->
//    <whitelist enabled="true">https://xxx.xxx.xxx,https://xxx.xxx.bbb</whitelist>
//</node>

import android.text.TextUtils;

public class ParamExternalHttpProxy extends ModuleParser {

    private String[] mWhiteList;
    private boolean mWhiteListEnabled;

    public ParamExternalHttpProxy() {
        super("external_proxy");
    }

    @Override
    protected void setExtras(String tag, String name, String val, boolean enabled) {
        if ("whitelist".equals(tag)) {
            mWhiteListEnabled = enabled;
            if (enabled && !TextUtils.isEmpty(val)) {
                mWhiteList = val.trim().split(",");
            }
        }
    }

    public String[] getWhiteList() {
        return mWhiteList;
    }

    public boolean isWhiteListEnabled() {
        return mWhiteListEnabled;
    }
}
