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

import android.content.Context;

import com.carota.core.IDisplayInfo;
import com.carota.core.ISession;
import com.carota.core.ITask;
import com.carota.html.DisplayInfo;
import com.momock.util.JsonHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class UpdateSession implements ISession {

    public static final String PROP_URL_SECURITY = "security_url";
    public static final String PROP_ULID = "ulid";
    public static final String PROP_VIN = "vin";
    public static final String PROP_USID = "usid";
    public static final String PROP_CONDITION = "cc";
    public static final String PROP_STRATEGY_RELEASE_NOTE = "strategy_desc/rn";
    public static final String PROP_MODE = "mode";
    public static final String PROP_OPERATION = "pre_operation";
    public static final String PROP_SCHEDULE_ID = "schedule_id";
    public static final String PROP_VMID = "vmid";
    public static final String PROP_CAMPAIGN_ID = "campaign_id";
    public static final String PROP_UPDATE_TIME = "update_time";
    public static final String DISPLAY_INFO_URL = "displayInfoUrl";

    private final boolean mFresh;
    private JSONObject mRawData;
    private JSONArray mRawTask;
    private UpdateTask[] mTasks;
    private int mTaskCount;
    private IDisplayInfo mDisplayInfo;

    public UpdateSession(JSONObject raw) {
        this(raw, true);
    }

    public UpdateSession(JSONObject raw, boolean fresh) {
        mFresh = fresh;
        if(null == raw) {
            return;
        }
        mRawData = raw;
        mRawTask = raw.optJSONArray("ecus");
        if(null != mRawTask) {
            mTaskCount = mRawTask.length();
            mTasks = new UpdateTask[mTaskCount];
            for (int i = 0; i < mTaskCount; i++) {
                mTasks[i] = new UpdateTask(i, this);
            }
        } else {
            mTaskCount = 0;
        }
    }

    public boolean isFresh() {
        return mFresh;
    }

    public boolean check() {
        return mTaskCount > 0;
    }

    public JSONObject getRawData() {
        return mRawData;
    }

    JSONObject getRawTask(int index) {
        return mRawTask.optJSONObject(index);
    }

    @Override
    public int getTaskCount() {
        return mTaskCount;
    }

    @Override
    public ITask getTask(int index) {
        return mTasks[index];
    }

    @Override
    public ITask getTask(String id) {
        for (ITask task : mTasks) {
            if (id.equals(task.getProp(ITask.PROP_NAME))) {
                return task;
            }
        }
        return null;
    }

    @Override
    public String getVinCode() {
        return mRawData.optString(PROP_VIN);
    }

    @Override
    public String getUSID() {
        return mRawData.optString(PROP_USID);
    }

    @Override
    public List<String> getCondition() {
        return JsonHelper.parseArray(getRawCondition(), String.class);
    }

    public JSONArray getRawCondition() {
        return mRawData.optJSONArray(PROP_CONDITION);
    }

    @Override
    public String getMode() {
        return mRawData.optString(PROP_MODE);
    }

    @Override
    public String getReleaseNote() {
        return JsonHelper.selectString(mRawData, PROP_STRATEGY_RELEASE_NOTE, "");
    }

    @Override
    public List<String> getOperation() {
        return JsonHelper.parseArray(mRawData.optJSONArray(PROP_OPERATION), String.class);
    }

    @Override
    public String getCampaignID() {
        return mRawData.optString(PROP_CAMPAIGN_ID);
    }

    @Override
    public String getScheduleID() {
        return mRawData.optString(PROP_SCHEDULE_ID);
    }

    @Override
    public long getUpdateTime() {
        return mRawData.optLong(PROP_UPDATE_TIME);
    }

    @Override
    public IDisplayInfo getDisplayInfo(Context context) {
        if (mDisplayInfo==null) mDisplayInfo = new DisplayInfo(context.getApplicationContext());
        return mDisplayInfo;
    }

    @SuppressWarnings("unchecked")
    public <T> T getProp(String name, T def) {
        Object val = mRawData.opt(name);
        if (def instanceof Long && val instanceof Number) {
            return (T) Long.valueOf(((Number) val).longValue());
        } else if (null != val && val.getClass().isInstance(def)) {
            return (T) val;
        }
        return def;
    }

    public String getDisplayInfoUrl() {
        return mRawData.optString(DISPLAY_INFO_URL);
    }

    @Override
    public String toString() {
        return mRawData.toString();
    }

}
