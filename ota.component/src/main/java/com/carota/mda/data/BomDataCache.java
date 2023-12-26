package com.carota.mda.data;

import android.content.Context;

import com.carota.mda.remote.info.BomInfo;
import com.carota.util.DatabaseHolderEx;
import com.momock.util.JsonDatabase;
import com.momock.util.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BomDataCache {
    private static final String KEY_BOM_INFO = "bom.info";
    private JsonDatabase.Collection mColCache;

    public BomDataCache(Context context) {
        mColCache = DatabaseHolderEx.getBom(context);
    }

    public void setBomInfo(List<BomInfo> info) {
        JSONObject root = new JSONObject();
        try {
            for (BomInfo bi : info) {
                root.put(bi.ID, BomInfo.toJson(bi));
            }
        } catch (JSONException je) {
            Logger.error(je);
        }
        mColCache.set(KEY_BOM_INFO, root);
    }

    public List<BomInfo> getBomInfoList() {
        JSONObject root = mColCache.get(KEY_BOM_INFO);
        List<BomInfo> ret = new ArrayList<>();
        if (root == null) {
            return ret;
        }
        Iterator<String> it = root.keys();
        while (it.hasNext()) {
            String name = it.next();
            try {
                ret.add(BomInfo.fromJson(root.optJSONObject(name)));
            } catch (JSONException je) {
                Logger.error(je);
            }
        }
        return ret;
    }
}
