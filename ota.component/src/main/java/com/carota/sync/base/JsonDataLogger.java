/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.sync.base;

import android.text.TextUtils;

import com.carota.mda.remote.info.EventInfo;
import com.carota.sync.base.DataLogger;
import com.momock.util.JsonDatabase;
import com.momock.util.Logger;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public abstract class JsonDataLogger extends DataLogger<JSONObject> {

    private final static String RECORD_DATA = "data";
    private final static String RECORD_NAME = "name";
    private final static String RECORD_ARRAY = "arr";
    private final static String RECORD_ID = "id";

    private JsonDatabase.Collection mCache;

    public JsonDataLogger(JsonDatabase.Collection col) {
        super();
        mCache = col;
    }

    protected boolean recordJsonData(String type, String id, JSONObject data, boolean isArray, boolean syncNow) {
        boolean ret = false;
        if (TextUtils.isEmpty(type) || TextUtils.isEmpty(id) || null == data) {
            Logger.error("[SYNC-JDL] Record : INVALID DATA @ " + type + "[" + id + "] " + data);
            return ret;
        }
        try {
            recordData(new JSONObject()
                    .put(RECORD_ID, id)
                    .put(RECORD_DATA, data)
                    .put(RECORD_ARRAY, isArray)
                    .put(RECORD_NAME, type), syncNow);
            ret = true;
        } catch (Exception e) {
            Logger.error("[SYNC-JDL] Record Er @ Meta");
        }
        return ret;
    }


    @Override
    protected IDataCache<JSONObject> onCreateDataCache() {
        return  new IDataCache<JSONObject>() {

            @Override
            public void remove(String key) {
                mCache.set(key, null);
            }

            @Override
            public void put(JSONObject data) {
                mCache.set(null, data);
            }

            @Override
            public List<Record<JSONObject>> list() {
                List<Record<JSONObject>> ret = new ArrayList<>();
                mCache.list(new JsonDatabase.IFilter() {
                    @Override
                    public boolean check(String id, JSONObject doc) {
                        ret.add(new Record<>(id, doc));
                        return true;
                    }
                }, false, 20);
                return ret;
            }
        };
    }

    @Override
    protected boolean onSyncData(JSONObject record) {
        String id = record.optString(RECORD_ID);
        String name = record.optString(RECORD_NAME);
        JSONObject data = record.optJSONObject(RECORD_DATA);

        if (id.isEmpty() || null == data || name.isEmpty()) {
            Logger.error("[SYNC-JDL] SYNC Er @ Doc");
            return true;
        }

        boolean isArray = record.optBoolean(RECORD_ARRAY);
        EventInfo ei = EventInfo.wrapEvent(data, 0, name, isArray);
        return send(id, ei);
    }

    abstract protected boolean send(String id, EventInfo ei);

}
