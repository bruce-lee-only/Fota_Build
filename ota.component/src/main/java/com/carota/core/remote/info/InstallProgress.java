/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.core.remote.info;

import com.momock.util.JsonHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class InstallProgress {

    public class Detail {
        public String name;
        public int state;
        public int progress;
    }

    public static final String TARGET_SLAVE = "slave";
    public static final String TARGET_MASTER = "master";

    private JSONObject mStatus;
    private JSONArray mDetail;
    private Map<String, Detail> mDetailCache;

    private InstallProgress(JSONObject status) {
        mStatus = status;
        if(null != status) {
            mDetail = status.optJSONArray("ret");
        }
        mDetailCache = null;
    }

    private InstallProgress check() {
        return null == mDetail ? null : this;
    }

    public int getTotalState() {
        return mStatus.optInt("state");
    }

    public String getTarget() {
        return mStatus.optString("target", TARGET_SLAVE);
    }

    public int getActiveCount() {
        return mDetail.length();
    }

    public synchronized Map<String, Detail> getDetail() {
        if(null == mDetailCache) {
            mDetailCache = new HashMap<>();
            for(int i = 0; i < mDetail.length(); i++) {
                JSONObject tmp = mDetail.optJSONObject(i);
                Detail d = new Detail();
                d.name = tmp.optString("name");
                d.progress = tmp.optInt("pg");
                d.state = tmp.optInt("state");
                mDetailCache.put(d.name, d);
            }
        }
        return mDetailCache;
    }

    public static InstallProgress create(JSONObject jo) {
        if(null != jo && jo.has("state")) {
            return new InstallProgress(jo).check();
        }
        return null;
    }

    @Override
    public String toString() {
        return null != mStatus ? mStatus.toString() : "EMPTY";
    }
}
