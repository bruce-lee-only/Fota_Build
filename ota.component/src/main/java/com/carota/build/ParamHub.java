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

public class ParamHub extends ModuleParser {

    /**
     * <node id="hub" type="rpc">
     *     <name>ota_proxy</name>
     *     <addr>127.0.0.1</addr>
     *     <port>20003</port>
     * </node>
     */

    public ParamHub() {
        super("hub");
    }

    @Override
    protected void setExtras(String tag, String name, String val, boolean enabled) {

    }

    public boolean isLegacy() {
        return "skt".equals(getType());
    }
}
