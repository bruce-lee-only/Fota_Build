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

public class ParamRSM extends ModuleParser{

    /**
     * <node id="rsm">
     *     </node><name>ota_rsm</name>
     * </node>
     */

    public ParamRSM() {
        super("rsm");
    }

    @Override
    protected void setExtras(String tag, String name, String val, boolean enabled) {

    }
}
