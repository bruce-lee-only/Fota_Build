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

//<node
//    enabled="true"
//    id="net_proxy">
//    <name>ota_net_proxy</name>
//    <whitelist>(live-ota-api.xxxxx.?),(ota-api.dflzm.?)</whitelist>
//</node>

import android.text.TextUtils;

public class ParamHttpProxy extends ModuleParser{

    private String[] mWhiteList;
    public ParamHttpProxy() {
        super("net_proxy");
    }

    @Override
    protected void setExtras(String tag, String name, String val, boolean enabled) {
        if ("whitelist".equals(tag)) {
            if (!TextUtils.isEmpty(val)) {
                mWhiteList = val.trim().split(",");
            }
        }
    }

    public String[] getWhiteList() {
        return mWhiteList;
    }
}
