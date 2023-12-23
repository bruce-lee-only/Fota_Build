/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.core.data;

import android.content.Context;

import com.carota.util.DatabaseHolderEx;
import com.momock.util.JsonDatabase;
import com.momock.util.Logger;

import org.json.JSONException;
import org.json.JSONObject;

public class DataCache {

    private static final String KEY_DATA_RESP = "data.resp";
    private static final String KEY_DATA_CAR = "data.car";

    private JsonDatabase.Collection mColCache;

    public DataCache(Context context) {
        // mColCache = DatabaseHolder.getCache(context);
        mColCache = DatabaseHolderEx.getAppCache(context);
    }

    public void setConnData(JSONObject resp) {
        mColCache.set(KEY_DATA_RESP, resp);
    }

    public JSONObject getConnData() {
        return mColCache.get(KEY_DATA_RESP);
    }

    public void setVehicleInfo(String vin) {
        JSONObject jo = new JSONObject();
        try {
            jo.put("vin", vin);
        } catch (JSONException e) {
            Logger.error(e);
            return;
        }
        mColCache.set(KEY_DATA_CAR, jo);
    }

    public String[] getVehicleInfo() {
        JSONObject info = mColCache.get(KEY_DATA_CAR);
        if(null != info) {
            return new String[]{info.optString("vin")};
        }
        return null;
    }
}
