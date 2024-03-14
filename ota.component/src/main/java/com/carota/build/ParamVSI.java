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

import android.content.Context;
import android.text.TextUtils;

import com.momock.util.Convert;

public class ParamVSI extends ModuleParser {

    /**
     * <node id="vsi" enabled="true">
     *     <name>ota_vsi_ivi</name>
     *     <pkg>com.carota.ai</pkg>       <!-- [OPT] -->
     * </node>
     */

    private String mPkg;

    public ParamVSI() {
        super("vsi");
    }

    @Override
    protected void setExtras(String tag, String name, String val, boolean enabled) {
        if(tag.equals("pkg")) {
            mPkg = val;
        }
    }

    public String getTargetPackage(Context context) {
        if(TextUtils.isEmpty(mPkg)) {
            return context.getPackageName();
        } else {
            return mPkg;
        }
    }
}
