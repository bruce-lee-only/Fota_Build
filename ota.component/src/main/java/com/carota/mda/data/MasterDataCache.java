/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.mda.data;

import android.content.Context;

import com.carota.mda.remote.info.EcuInfo;
import com.carota.util.DatabaseHolderEx;
import com.momock.util.JsonDatabase;
import com.momock.util.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MasterDataCache {

    private static final String KEY_DATA_CONN = "conn.data";
    private static final String KEY_ECU_INFO = "ecu.info";
    private JsonDatabase.Collection mColCache;

    public MasterDataCache(Context context) {
        // mColCache = DatabaseHolder.getCache(context);
        mColCache = DatabaseHolderEx.getAppCache(context);
    }

    public void setConnData(JSONObject resp) {
        mColCache.set(KEY_DATA_CONN, resp);
    }

    public JSONObject getConnData() {
        return mColCache.get(KEY_DATA_CONN);
    }

    public void setEcuInfo(List<EcuInfo> info) {
        JSONObject root = new JSONObject();
        try {
            for (EcuInfo ei : info) {
                root.put(ei.ID, EcuInfo.toJson(ei));
            }
        } catch (JSONException je) {
            Logger.error(je);
        }
        mColCache.set(KEY_ECU_INFO, root);
    }

    public List<EcuInfo> getEcuInfoList() {
        JSONObject root = mColCache.get(KEY_ECU_INFO);
        Iterator<String> it = root.keys();
        List<EcuInfo> ret = new ArrayList<>();
        while (it.hasNext()) {
            String name = it.next();
            try {
                ret.add(EcuInfo.fromJson(root.optJSONObject(name)));
            } catch (JSONException je) {
                Logger.error(je);
            }
        }
        return ret;
    }

}
