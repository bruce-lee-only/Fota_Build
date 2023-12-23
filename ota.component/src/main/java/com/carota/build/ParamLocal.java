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

import android.text.TextUtils;

import com.momock.util.Convert;

import java.util.HashMap;
import java.util.Map;

public class ParamLocal extends ConfigParser {

    /**
     * <carota>
     *     <port>20002</port>
     *     <psrv>https://api.carota.ai</psrv>
     *     <tsrv>https://test.carota.ai</tsrv>
     * </carota>
     */

    private int mPort;
    private Map<String, String> mExtra;

    public ParamLocal() {
        super(null);
        mPort = 0;
        mExtra = new HashMap<>();
    }

    @Override
    protected void set(String tag, String name, String val, boolean enabled) {
        if(null == tag || TextUtils.isEmpty(val)) {
            return;
        }
        if("port".equals(tag)) {
            mPort = Convert.toInteger(val);
        } else {
            mExtra.put(tag, val);
        }
    }

    public int getPort() {
        return mPort;
    }

    public String getProductionBaseUrl() {
        return mExtra.get("psrv");
    }

    public String getTestBaseUrl() {
        return mExtra.get("tsrv");
    }
}
