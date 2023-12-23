/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.dtc.log.data;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Instruction {

    private JSONObject mRaw;
    private JSONObject mData;
    private JSONObject mUrls;
    private JSONObject mLc;

    public Instruction(JSONObject raw) {
        mRaw = raw;
        mData = mRaw.optJSONObject("data");
        if (mData != null) {
            mUrls = mData.optJSONObject("urls");
            mLc = mData.optJSONObject("lc");
        }
    }

    public String getToken() {
        return mData.optString("token");
    }

    public boolean hasData() {
        return mData != null;
    }

    public String getVin() {
        return mData.optString("vin");
    }

    public boolean hasComplete() {
        return mData.optBoolean("scheduleComplete", false);
    }

    public String getMqttUrl() {
        return mUrls.optString("mqtt");
    }

    public String getFileUrl() {
        return mUrls.optString("fileUrl");
    }

    public int getModel() {
        return mData.optInt("model");
    }

    public int getCode() {
        return mRaw.optInt("code");
    }

    public String getMsg() {
        return mRaw.optString("msg");
    }

    public boolean getSuccess() {
        return mRaw.optBoolean("success");
    }

    public List<LogTask> getLogTask () {
        List<LogTask> ret = new ArrayList<>();
        JSONArray ja = mLc.optJSONArray("rules");
        if(null != ja) {
            for (int i = 0; i < ja.length(); i++) {
                ret.add(new LogTask(ja.optJSONObject(i)));
            }
        }
        return ret;
    }
}
