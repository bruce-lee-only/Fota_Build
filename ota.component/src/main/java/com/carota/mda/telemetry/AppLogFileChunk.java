/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/
package com.carota.mda.telemetry;

import com.momock.util.JsonDatabase;
import com.momock.util.Logger;

import org.json.JSONObject;

public class AppLogFileChunk {
    private static final String STATE_KEY = "app_log_state";
    private static final String TYPE_KEY = "app_log_type";
    // 1:客户端日志文件, 2:车端升级日志文件, 3:车端没有usid/ulid的日志类型,4:车端错误日志上报或升级过程上报
    public static final int TYPE_CLIENT = 1;
    public static final int TYPE_VEHICLE = 2;
    public static final int TYPE_NO_ULID = 3;
    public static final int TYPE_UPDATE = 4;
    private JsonDatabase.Collection mCollection;

    public AppLogFileChunk(JsonDatabase.Collection collection) {
        this.mCollection = collection;
    }

    public void updateState(int state) {
        Logger.debug("AppLogFileChunk updateState = " + state);
        try {
            JSONObject j = mCollection.get(STATE_KEY);
            if (j == null) j = new JSONObject();
            j.put(STATE_KEY, state);
            mCollection.set(STATE_KEY, j);
        } catch (Exception e) {
            Logger.debug("AppLogFileChunk updateState Exception = " + e);
        }
    }

    public int queryState() {
        JSONObject j = mCollection.get(STATE_KEY);
        return j != null ? j.optInt(STATE_KEY, -1) : -1;
    }

    public void updateType(int type) {
        Logger.debug("AppLogFileChunk updateType = " + type);
        try {
            JSONObject j = mCollection.get(TYPE_KEY);
            if (j == null) j = new JSONObject();
            j.put(TYPE_KEY, type);
            mCollection.set(TYPE_KEY, j);
        } catch (Exception e) {
            Logger.debug("AppLogFileChunk updateState Exception = " + e);
        }
    }

    public int queryType() {
        JSONObject j = mCollection.get(TYPE_KEY);
        return j != null ? j.optInt(TYPE_KEY, 1) : 1;
    }

    public void cleanState() {
        mCollection.set(STATE_KEY, null);
    }
}
