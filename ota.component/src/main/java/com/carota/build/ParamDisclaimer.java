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

import java.util.HashMap;
import java.util.Map;

public class ParamDisclaimer extends ConfigParser {

    /**
     * <node id="disclaimer">
     *     <url>xxx</url>
     *     <retry>0</retry>            <!-- [OPT] -->
     * </node>
     */

    private Map<String, String> mExtra;

    public ParamDisclaimer() {
        super("disclaimer");
        mExtra = new HashMap<>();
    }

    @Override
    protected void set(String tag, String name, String val, boolean enabled) {
        if(null != tag && null != val) {
            mExtra.put(tag, val);
        }
    }

    public String getUrl() {
        return mockUrl(mExtra.get("url"));
    }

    public int getMaxRetry() {
        return Convert.toInteger(mExtra.get("retry"), 3);
    }
}
