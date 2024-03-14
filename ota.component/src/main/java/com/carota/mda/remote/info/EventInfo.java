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

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class EventInfo {

    public final String TYPE;
    private Object mPayload;
    private long mRecordTime;

    private EventInfo(String type, JSONArray payload, long recordTime) {
        TYPE = type;
        mPayload = payload;
        mRecordTime = recordTime;
    }

    private EventInfo(String type, JSONObject payload, long recordTime, boolean isArray) {
        TYPE = type;
        if(isArray) {
            mPayload = new JSONArray().put(payload);
        } else {
            mPayload = payload;
        }
        mRecordTime = recordTime;
    }

    public long getRecordTime() {
        return mRecordTime;
    }

    public Object getPayload() {
        return mPayload;
    }

    /**
     "type": {INFO}
     */
    public static EventInfo wrapEvent(JSONObject eventInfo, long recordTime, String type, boolean isArray) {
        if(!TextUtils.isEmpty(type)) {
            return new EventInfo(type, eventInfo,
                    recordTime > 0 ? recordTime : System.currentTimeMillis(), isArray);
        }
        return null;
    }

    public static <T extends Object>EventInfo wrapEvent(List<T> eventInfo, long recordTime, String type) {
        if(!TextUtils.isEmpty(type)) {
            return new EventInfo(type, new JSONArray(eventInfo),
                    recordTime > 0 ? recordTime : System.currentTimeMillis());
        }
        return null;
    }

    /**
     "vupdate": [{
     	"ecu": "MCU",
     	"state": 4
     }],
     */
    public static EventInfo wrapStateEvent(List<JSONObject> stateInfo, long recordTime) {
        if(stateInfo.size() > 0) {
            JSONObject tester = stateInfo.get(0);
            String name = tester.optString("ecu");
            int state = tester.optInt("state", Integer.MIN_VALUE);
            if (!TextUtils.isEmpty(name) && Integer.MIN_VALUE != state) {
                return EventInfo.wrapEvent(stateInfo, recordTime, "vupdate");
            }
        }
        return null;
    }
}
