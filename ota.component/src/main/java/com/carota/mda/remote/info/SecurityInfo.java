/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.mda.remote.info;

import org.json.JSONArray;
import org.json.JSONObject;

public class SecurityInfo {

    /**
     {
     "type":"carota",
     "secret":["src psw", "dst psw"],
     "cert":["base64", "base64"],
     }
     **/
    private JSONObject mRaw;

    private SecurityInfo(JSONObject raw) {
        mRaw = raw;
    }

    public String getType() {
        return mRaw.optString("type");
    }

    private String getDataFromArray(int index, String key) {
        JSONArray ja = mRaw.optJSONArray(key);
        return null != ja ? ja.optString(index, null) : null;
    }

    public String getSecret(int index) {
        return getDataFromArray(index, "secret");
    }

    public String getCertificate(int index) {
        return getDataFromArray(index, "cert");
    }

    public static SecurityInfo create(JSONObject resp, int arrSize) {
        SecurityInfo si = new SecurityInfo(resp);
        int index = arrSize - 1;
        if(null != si.getCertificate(index) || null != si.getSecret(index)) {
            return si;
        }
        return null;
    }
}
