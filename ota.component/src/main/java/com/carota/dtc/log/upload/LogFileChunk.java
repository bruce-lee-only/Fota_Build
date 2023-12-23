/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/
package com.carota.dtc.log.upload;

import com.momock.util.JsonDatabase;
import com.momock.util.Logger;

import org.json.JSONObject;


public class LogFileChunk {
    private static final String STATE_KEY = "dtc_state";
    private JsonDatabase.Collection mCollection;

    public LogFileChunk(JsonDatabase.Collection collection) {
        this.mCollection = collection;
    }

    public void updateState(int state) {
        Logger.debug("LogFileChunk updateState = " + state);
        try {
            JSONObject j = mCollection.get(STATE_KEY);
            if (j == null) j = new JSONObject();
            j.put(STATE_KEY, state);
            mCollection.set(STATE_KEY, j);
        } catch (Exception e) {
            Logger.debug("LogFileChunk updateState Exception = " + e);
        }
    }
    public int queryState() {
        JSONObject j = mCollection.get(STATE_KEY);
        return j != null ? j.optInt(STATE_KEY, -1) : -1;
    }

    public void cleanState() {
        mCollection.set(STATE_KEY, null);
    }
}
