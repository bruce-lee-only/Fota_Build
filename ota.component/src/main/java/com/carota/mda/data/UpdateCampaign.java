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

import android.text.TextUtils;

import com.carota.core.ISession;
import com.carota.mda.remote.info.BomInfo;
import com.momock.util.JsonHelper;
import com.momock.util.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UpdateCampaign {

    public static final String PROP_URL_FILE = "file_url";
    public static final String PROP_URL_CFG = "config_url";
    public static final String PROP_URL_SECURITY = "security_url";
    public static final String PROP_URL_TOKEN = "token_url";
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
    public static final String PROP_PKI_TYPE = "pki_type";
    public static final String PROP_LOG_TYPE = "log_type";
    public static final String PROP_LOG_PATH = "log_path";

    //上报日志类型,-1为不上传日志，1为车端日志打开，4为错误上传,5为完成上传
    public static final int UPLOAD_LOG_NONE = -1;
    public static final int UPLOAD_LOG_VEHICLE = 1;
    public static final int UPLOAD_LOG_WHEN_UPDATE_ERROR = 4;
    public static final int UPLOAD_LOG_WHEN_UPDATE_FINISH = 5;
    public static final String PROP_UPDATE_TIME = "update_time";

    private JSONObject mRawData;
    private JSONArray mRawItem;
    private UpdateItem[] mItems;
    private int mItemCount;
    private List<BomInfo> mBomInfoList;

    public UpdateCampaign(JSONObject raw, List<BomInfo> bomInfoList) {
        if(null == raw) {
            return;
        }
        mBomInfoList = bomInfoList;
        mRawData = raw;
        mRawItem = raw.optJSONArray("ecus");
        if(null != mRawItem) {
            mItemCount = mRawItem.length();
            mItems = new UpdateItem[mItemCount];
            for (int i = 0; i < mItemCount; i++) {
                mItems[i] = new UpdateItem(i, this);
            }
        } else {
            mItemCount = 0;
        }
        setElecToSession();
    }

    private void setElecToSession() {
        try {
            if (getItemCount()==0) return;
            if (getOperation().contains(ISession.OPERATE_EIC_OFF)) return;
            List<String> bomHvoInfos = getBomHvoInfos();
            if (bomHvoInfos.isEmpty())return;
            for (UpdateItem item:mItems) {
                if (bomHvoInfos.contains(item.getProp(UpdateItem.PROP_NAME, ""))) {
                    JSONArray array = mRawData.optJSONArray(PROP_OPERATION);
                    if (array==null) array = new JSONArray();
                    array.put(ISession.OPERATE_EIC_OFF);
                    mRawData.put(PROP_OPERATION, array);
                    return;
                }
            }
        } catch (Exception e) {
            Logger.error(e);
        }
    }

    public boolean check() {
        return mItemCount > 0;
    }

    public JSONObject getRawData() {
        return mRawData;
    }

    JSONObject getRawItem(int index) {
        return mRawItem.optJSONObject(index);
    }

    public String getUrl(String tag, String id) {
        return mRawData.optString(tag).replace("{id}", id);
    }

    public String getSecurityUrl() {
        return mRawData.optString(PROP_URL_SECURITY);
    }

    public String getTokenUrl() {
        return mRawData.optString(PROP_URL_TOKEN);
    }

    public String getPropPkiType() {return mRawData.optString(PROP_PKI_TYPE);}

    public String getULID() {
        return mRawData.optString(PROP_ULID);
    }

    public int getItemCount() {
        return mItemCount;
    }

    public UpdateItem getItem(int index) {
        return mItems[index];
    }

    public UpdateItem getItem(String id) {
        for (UpdateItem item : mItems) {
            if (id.equals(item.getProp(UpdateItem.PROP_NAME, null))) {
                return item;
            }
        }
        return null;
    }

    public String getVinCode() {
        return mRawData.optString(PROP_VIN);
    }

    public String getUSID() {
        return mRawData.optString(PROP_USID);
    }

    public List<String> getCondition() {
        return JsonHelper.parseArray(getRawCondition(), String.class);
    }

    public JSONArray getRawCondition() {
        return mRawData.optJSONArray(PROP_CONDITION);
    }

    public String getMode() {
        return mRawData.optString(PROP_MODE);
    }

    public String getReleaseNote() {
        return JsonHelper.selectString(mRawData, PROP_STRATEGY_RELEASE_NOTE, "");
    }

    public String getCampaignId(){
        return mRawData.optString(PROP_CAMPAIGN_ID);
    }

    public String getScheduleId() {
        return mRawData.optString(PROP_SCHEDULE_ID);
    }

    public List<String> getOperation() {
        return JsonHelper.parseArray(mRawData.optJSONArray(PROP_OPERATION), String.class);
    }

    public int getUpdateTime(){
        return mRawData.optInt(PROP_UPDATE_TIME);
    }

    @SuppressWarnings("unchecked")
    public <T> T getProp(String name, T def) {
        Object val = mRawData.opt(name);
        if(def instanceof Long && val instanceof Number) {
            return (T) Long.valueOf(((Number) val).longValue());
        } else if(null != val && val.getClass().isInstance(def)) {
            return (T) val;
        }
        return def;
    }

    public String getProp(String name) {
        return mRawData.optString(name);
    }

    @Override
    public String toString() {
        return mRawData.toString();
    }

    /**
     * There may be multiple identical ECUs
     * @param name
     * @return
     */
    public List<BomInfo> getBomInfos(String name) {
        List<BomInfo> list = new ArrayList<>();
        if (mBomInfoList==null|| TextUtils.isEmpty(name)) return list;

        for (BomInfo bomInfo: mBomInfoList) {
            if(name.equals(bomInfo.getName())) {
                list.add(bomInfo);
            }
        }
        return list;
    }
    private List<String> getBomHvoInfos() {
        List<String> list = new ArrayList<>();
        if (mBomInfoList==null) return list;

        for (BomInfo bomInfo: mBomInfoList) {
            if (bomInfo.getFlashConfig().getHvo()==1){
                list.add(bomInfo.getName());
            }
        }
        return list;
    }

    public BomInfo getBomInfo(String name) {
        List<BomInfo> info = getBomInfos(name);
        if (info.size()>0) return info.get(0);
        return null;
    }



    public int getLogType() {
        return mRawData.optInt(PROP_LOG_TYPE, -1);
    }

    public HashMap<String, String> getLogPath() {
        HashMap<String, String> map = new HashMap<>();
        JSONArray array = mRawData.optJSONArray(PROP_LOG_PATH);
        if (array != null) {
            try {
                for (int i = 0; i < array.length(); i++) {
                    String[] item = array.getString(i).split(":");
                    if (item !=null && item.length == 2) {
                        map.put(item[0], item[1]);
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return map;
    }
}