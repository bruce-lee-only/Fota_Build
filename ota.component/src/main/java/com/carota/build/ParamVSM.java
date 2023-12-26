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

import java.util.HashMap;
import java.util.Map;

public class ParamVSM extends ConfigParser {

    /**
     * <node id="vsm">
     *     <event>vsi_ivi</event>
     *     <info>vsi_ivi</info>
     *     <condition>vsi_ivi</condition>
     *     <power>vsi_power</power>
     * </node>
     */

    private Map<String, String> mExtra;

    public ParamVSM() {
        super("vsm");
        mExtra = new HashMap<>();
    }

    @Override
    protected void set(String tag, String name, String val, boolean enabled) {
        if(null == name && null != tag && null != val) {
            mExtra.put(tag, val);
        }
    }

    public String getEvent() {
        return mExtra.get("event");
    }

    public String getInfo() {
        return mExtra.get("info");
    }

    public String getCondition() {
        return mExtra.get("condition");
    }

    public String getPower() {
        return mExtra.get("power");
    }
}
