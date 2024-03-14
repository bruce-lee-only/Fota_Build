/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.core.data;

import com.carota.core.ITask;

import org.json.JSONObject;

public class UpdateTask implements ITask {

    public static final String PROP_SRC_VER = "sv";
    public static final String PROP_DST_VER = "tv";
    public static final String PROP_NAME = "name";
    public static final String PROP_RELEASE_NOTE = "rn";
    public static final String PROP_DST_SIZE = "d_size";
    public static final String PROP_SRC_SIZE = "s_size";

    private UpdateSession mSession;
    private int mIndex;
    private int mDmProgress;
    private int mDmState;
    private int mInsProgress;
    private int mInsState;
    private int mDmSpeed;

    public UpdateTask(int index, UpdateSession session) {
        mIndex = index;
        mSession = session;
        mDmProgress = 0;
        mDmState = 0;
    }

    public JSONObject getData() {
        return mSession.getRawTask(mIndex);
    }

    public boolean check() {
        JSONObject jo = getData();
        return null != jo && jo.has("id")
                && jo.has("cid") && jo.has("smd5")
                && jo.has("dmd5");
    }

    @Override
    public int getIndex() {
        return mIndex;
    }

    @Override
    public int getDownloadProgress() {
        return mDmProgress;
    }
    
    @Override
    public int getDownloadSpeed() {
        return mDmSpeed;
    }

    @Override
    public int getDownloadState() {
        return mDmState;
    }

    @Override
    public int getInstallState() {
        return mInsState;
    }

    @Override
    public int getInstallProgress() {
        return mInsProgress;
    }

    @Override
    @Deprecated
    public String getProp(String name) {
        return getData().optString(name, "");
    }

    @Override
    public String getSrcVer() {
        return getProp(PROP_SRC_VER, "");
    }

    @Override
    public String getDstVer() {
        return getProp(PROP_DST_VER, "");
    }

    @Override
    public long getPackageSize() {
        return getProp(PROP_DST_SIZE, 0L);
    }

    @Override
    public String getReleaseNote() {
        return getProp(PROP_RELEASE_NOTE, "");
    }

    @SuppressWarnings("unchecked")
    @Override
    @Deprecated
    public <T> T getProp(String name, T def) {
        Object val = getData().opt(name);
        if(def instanceof Long && val instanceof Number) {
            return (T) Long.valueOf(((Number) val).longValue());
        } else if(null != val && val.getClass().isInstance(def)) {
            return (T) val;
        }
        return def;
    }

    public void setInstallState(int state, int progress) {
        mInsState = state;
        mInsProgress = progress;
    }

    public void setDownloadProgress(int state, int pg) {
        mDmState = state;
        mDmProgress = pg;
    }
    public void setDownloadSpeed(int downloadSpeed) {
        this.mDmSpeed = downloadSpeed;
    }
}
