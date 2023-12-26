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
//</node>

public class ParamHttpProxy extends ModuleParser{
    public ParamHttpProxy() {
        super("net_proxy");
    }

    @Override
    protected void setExtras(String tag, String name, String val, boolean enabled) {

    }
}
