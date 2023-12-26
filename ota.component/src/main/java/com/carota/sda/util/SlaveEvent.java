/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.sda.util;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.carota.util.DatabaseHolderEx;
import com.momock.util.JsonDatabase;
import com.momock.util.JsonHelper;
import com.momock.util.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SlaveEvent {

    private static final Map<String, SlaveEvent> sCachePool = new HashMap<>();

    public static synchronized SlaveEvent getCache(Context context, String slaveName) {
        if (TextUtils.isEmpty(slaveName)) {
            Logger.error("EMPTY NAME @ SlaveEvent");
            return null;
        }
        SlaveEvent cache = sCachePool.get(slaveName);
        if(null == cache) {
            cache = new SlaveEvent(context, slaveName);
            sCachePool.put(slaveName, cache);
        }
        return cache;
    }

    private static final String KEY_TYPE = "type";
    private static final String KEY_EVENT = "event";
    private static final String KEY_EXTRA = "extra";
    public final String NAME;
    private JsonDatabase.Collection mSlaveCol;

    private SlaveEvent(Context context, String slaveName) {
        NAME = slaveName;
        // mSlaveCol = DatabaseHolder.getSlaveEvent(context, slaveName);
        mSlaveCol = DatabaseHolderEx.getSlaveEvent(context, slaveName);
        mSlaveCol.setCachable(false);
    }

    public boolean fetchEvent(final String type, int max, List<String> events, List<String> ids) {
        if(null == events || null == ids) {
            return false;
        }
        JsonDatabase.IFilter filter = null;
        if(!TextUtils.isEmpty(type)) {
            filter = new JsonDatabase.IFilter() {
                @Override
                public boolean check(String id, JSONObject doc) {
                    return doc.optString(KEY_TYPE).equals(type);
                }
            };
        }
        List<JsonDatabase.Document> docs = mSlaveCol.list(filter, false, max);
        JSONObject data;
        String docId;
        for(JsonDatabase.Document d : docs){
            data = d.getData();
            docId = d.getId();
            if(null != data) {
                events.add(data.toString());
            } else {
                Logger.error("EventCache : Invalid DATA @ " + docId);
                mSlaveCol.set(docId, null);
                continue;
            }
            ids.add(d.getId());
        }
        return true;
    }

    public boolean deleteEvent(List<String> ids) {
        if(null == ids) {
            return false;
        }
        for(String id : ids){
            mSlaveCol.set(id, null);
        }
        return true;
    }

    public boolean logEvent(String type, String event, Bundle extra) {
        try {
            JSONObject json = new JSONObject();
            if(!TextUtils.isEmpty(type)) {
                json.put(KEY_TYPE, type);
            }
            json.put(KEY_EVENT, event);
            if (null != extra) {
                json.put(KEY_EXTRA, JsonHelper.parse(extra));
            }
            mSlaveCol.set(null, json);
            return true;
        } catch (JSONException e) {
            //Handle exception here
        }
        return false;
    }
}
