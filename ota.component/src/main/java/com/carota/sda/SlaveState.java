/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.sda;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

public final class SlaveState {

    public static final String STATE_IDLE = "idle";
    public static final String STATE_DOWNLOAD = "download";
    public static final String STATE_UPGRADE = "upgrade";
    public static final String STATE_ROLLBACK = "rollback";
    public static final String STATE_SUCCESS = "success";
    public static final String STATE_FAILURE = "failure";
    public static final String STATE_ERROR = "error";

    public final String name;
    public final int domain;
    private String mState;
    private int mProgress;
    private String msg;
    private int mErrorCode;


    public SlaveState(String name, int domain) {
        this.name = name;
        this.domain = domain;
        mState = STATE_IDLE;
        mProgress = 0;
        msg = "";
    }

    public String getState() {
        return mState;
    }

    public boolean isRunning() {
        return STATE_DOWNLOAD.equals(mState)
                || STATE_UPGRADE.equals(mState)
                || STATE_ROLLBACK.equals(mState);
    }

    public int getProgress() {
        return mProgress;
    }

    synchronized SlaveState setState(String state, int errorCode) {
        if(!TextUtils.isEmpty(state)) {
            mState = state;
        }
        mErrorCode = errorCode;
        return this;
    }

    void setProgress(int progress) {
        mProgress = progress;
    }

    public static JSONObject toJson(SlaveState ss) throws JSONException {
        return new JSONObject()
                .put("name", ss.name)
                .put("domain", ss.domain)
                .put("status", ss.mState)
                .put("msg", ss.msg)
                .put("progress", ss.mProgress);
    }

    public static SlaveState fromJson(JSONObject data) throws JSONException {
        SlaveState task = new SlaveState(data.getString("name"), data.getInt("domain"));
        task.mState = data.getString("status");
        task.mProgress = data.optInt("progress");
        task.msg = data.optString("msg");
        return task;
    }

    public String getMsg() {
        return msg;
    }

    public SlaveState setMsg(String msg) {
        if (!TextUtils.isEmpty(msg))
            this.msg = msg;
        return this;
    }

    public int getErrorCode() {
        return mErrorCode;
    }
}
