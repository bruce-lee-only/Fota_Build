package com.carota.build;

import com.momock.util.Convert;

import java.util.HashMap;
import java.util.Map;

/*******************************************************************************
 * Copyright (C) 2022-2025 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/
public class ParamConfig extends ModuleParser {

    private String mHost;
    private Map<String, String> mExtra;

    /**
     * <node id="config">
     *     <name></name>
     *     <addr></addr>
     *     <port></port>
     *     <config></config>
     *     <retry></retry>
     * </node>
     *
     */
    public ParamConfig() {
        super("config");
        mExtra = new HashMap<>();
    }

    @Override
    protected void setExtras(String tag, String name, String val, boolean enabled) {
        if(null != tag && null != val) {
            mExtra.put(tag, val);
        }
    }

    public String getConfigUrl() {
        return mockUrl(mExtra.get("config"));
    }

    public int getMaxRetry() {
        return Convert.toInteger(mExtra.get("retry"), 3);
    }
}
