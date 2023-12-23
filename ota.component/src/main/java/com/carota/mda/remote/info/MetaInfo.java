package com.carota.mda.remote.info;

import org.json.JSONObject;

public class MetaInfo {
    /**
     * {
     * "sign":"",
     * "type":1,
     * "t":"97573902975facafcb0331cf79e94c63",
     * "d":"97573902975facafcb0331cf79e94c63",
     * "s":"97573902975facafcb0331cf79e94c63"
     * }
     */

    public static final String PROP_TGT = "t";
    public static final String PROP_DST = "d";
    public static final String PROP_SRC = "s";
    private JSONObject mRaw;

    public MetaInfo(JSONObject raw) {
        mRaw = raw;
    }

    public String getSignMethod() {
        return mRaw.optString("sign");
    }

    public String getType() {
        return mRaw.optString("type");
    }

    public String getTargetSign() {
        return mRaw.optString(PROP_TGT);
    }

    public String getDestinationSign() {
        return mRaw.optString(PROP_DST);
    }

    public String getSourceSign() {
        return mRaw.optString(PROP_SRC);
    }

    public String getValue(String key) {
        return mRaw.optString(key);
    }
}
