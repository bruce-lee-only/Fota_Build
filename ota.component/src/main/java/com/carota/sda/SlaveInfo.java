/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.sda;

import android.os.Bundle;

import com.carota.agent.RemoteAgent;
import com.momock.util.JsonHelper;
import com.momock.util.Logger;

import org.json.JSONObject;

public final class SlaveInfo {

    public static final String PROP_VER_DA = "dav";
    public static final String PROP_VER_UA = RemoteAgent.KEY_UA_VER;
    public static final String PROP_VER_HW = RemoteAgent.KEY_HARDWARE_VER;
    public static final String PROP_VER_SW = RemoteAgent.KEY_SOFTWARE_VER;
    public static final String PROP_SN = RemoteAgent.KEY_SERIAL_NUMBER;

    private JSONObject mData;

    public SlaveInfo(JSONObject data) {
        if (null != data && data.has(PROP_VER_SW)) {
            mData = data;
        } else {
            mData = new JSONObject();
        }
    }

    public SlaveInfo setProp(String key, String val) {
        try {
            mData.put(key, val);
        } catch (Exception e) {
            Logger.error(e);
        }
        return this;
    }

    public String getProp(String prop) {
        return mData.optString(prop);
    }

    public static SlaveInfo fromJson(JSONObject data) {
        return new SlaveInfo(data);
    }

    public static SlaveInfo fromBundle(Bundle data) {
        return fromJson(JsonHelper.parse(data));
    }

    public static JSONObject toJson(SlaveInfo si) {
        return si.mData;
    }

    @Override
    public String toString() {
        return mData.toString();
    }
}
