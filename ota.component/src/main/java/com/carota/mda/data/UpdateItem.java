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

import com.carota.mda.remote.info.BomInfo;

import org.json.JSONObject;

public class UpdateItem {

    public static final String PROP_SRC_VER = "sv";
    public static final String PROP_DST_VER = "tv";
    public static final String PROP_NAME = "name";
    public static final String PROP_CID = "cid";
    public static final String PROP_SRC_MD5 = "smd5";
    public static final String PROP_DST_MD5 = "dmd5";
    public static final String PROP_ID = "id";
    public static final String PROP_CFG_ENABLE = "config";
    public static final String PROP_RELEASE_NOTE = "rn";
    public static final String PROP_DST_SIZE = "d_size";
    public static final String PROP_SRC_SIZE = "s_size";
    public static final String PROP_TIME = "time";
    public static final String PROP_DESCRIPTOR = "descriptor";
    public static final String PROP_DOMAIN = "domain";
    public static final String PROP_GROUP = "group";
    public static final String PROP_STEP = "step";
    public static final String PROP_UPDATE_TIME = "update_time";
    public static final String PROP_LINE = "line";
    public static final String PROP_HAS_SECURITY = "hasSecurity";


    private final UpdateCampaign mSession;
    private final int mIndex;

    public UpdateItem(int index, UpdateCampaign session) {
        mIndex = index;
        mSession = session;
    }

    private JSONObject getData() {
        return mSession.getRawItem(mIndex);
    }

    public boolean check() {
        JSONObject jo = getData();
        return null != jo && jo.has(PROP_NAME) && jo.has(PROP_DST_MD5);
    }

    public int getIndex() {
        return mIndex;
    }

    @SuppressWarnings("unchecked")
    public <T> T getProp(String name, T def) {
        Object val = getData().opt(name);
        if(def instanceof Long && val instanceof Number) {
            return (T) Long.valueOf(((Number) val).longValue());
        } else if(null != val && val.getClass().isInstance(def)) {
            return (T) val;
        }
        return def;
    }

    public String getProp(String name) {
        return getData().optString(name, "");
    }

}
