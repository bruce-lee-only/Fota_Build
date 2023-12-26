package com.carota.mda.remote.info;

import org.json.JSONObject;

public class TokenInfo {

    /**
     * {
     * "md5": "b5b4e06dd839c71852d9069310817de8",
     * "file_url": "http://127.0.0.1:8080/files/{id}"
     * }
     */
    private JSONObject mRaw;

    public TokenInfo(JSONObject raw) {
        mRaw = raw;
    }

    public String getMd5() {
        return mRaw.optString("md5");
    }

    public String getFileUrl(String id) {
        return mRaw.optString("file_url").replace("{id}", id);
    }

    public <T> T getProp(String name, T def) {
        Object val = mRaw.opt(name);
        if(def instanceof Long && val instanceof Number) {
            return (T) Long.valueOf(((Number) val).longValue());
        } else if(null != val && val.getClass().isInstance(def)) {
            return (T) val;
        }
        return def;
    }
}
