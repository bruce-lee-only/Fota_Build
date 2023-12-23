/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/
package com.carota.sota.store;

import org.json.JSONObject;

public class UpdateItem extends AppInfo{

    public static final String PROP_PACKAGE_NAME = "pn";
    public static final String PROP_VER_CODE = "vc";
    public static final String PROP_VER_NAME = "vn";
    public static final String PROP_FILE_MD5 = "md5";
    public static final String PROP_SCHEDULE_ID = "schedule";
    public static final String PROP_ID = "id";

    private UpdateCampaign mCampaign;
    private int mIndex;

    UpdateItem(int index, UpdateCampaign session) {
        super(null, 0, null);
        mIndex = index;
        mCampaign = session;
    }

    private JSONObject getData() {
        return mCampaign.getRawItem(mIndex);
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

    @Override
    public String getPackageName() {
        return getProp(PROP_PACKAGE_NAME);
    }

    @Override
    public int getVersionCode() {
        return getProp(PROP_VER_CODE, 0);
    }

    @Override
    public String getVersionName() {
        return getProp(PROP_VER_NAME);
    }

    String getId() {
        return getProp(PROP_ID);
    }

    int getSchedule() {
        return getProp(PROP_SCHEDULE_ID, 0);
    }

    String getMd5() {
        return getProp(PROP_FILE_MD5);
    }
}
