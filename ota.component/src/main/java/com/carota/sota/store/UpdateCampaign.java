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

import com.carota.sota.util.RequestState;
import com.momock.util.JsonHelper;

import org.json.JSONArray;
import org.json.JSONObject;

/*
{
    "code":100,
    "msg":"",
    "data": {
        "vmid":"",
        "file_url":"https://api.carota.ai/dl?id={id}",
        "apps":[
            {
                "pn":"com.apk.dummy1",
                "vc":123456,
                "md5":"adsf123",
                "schedule":1,
                "id":"5fa13c9008c41ab4098c73ea",
                "vn":"2.1.8"
            },
            {
                "pn":"com.apk.dummy2",
                "vc":1234567,
                "md5":"adsf123",
                "schedule":2,
                "id":"5fa13c9008c41ab4098c73ea",
                "vn":"2.1.9"
            }
        ]
    }
}
}*/

public class UpdateCampaign {

    private RequestState mReqState;
    private String mVmId;
    private String mFileUrlTemplate;
    private JSONArray mRawItem;
    private UpdateItem[] mItems;


    public static UpdateCampaign parseCampaign(String data) {
        JSONObject raw = JsonHelper.parseObject(data);
        if(null == raw) {
            return null;
        }
        JSONObject payload = raw.optJSONObject("data");
        int code = raw.optInt("code");
        String msg = raw.optString("msg");
        return new UpdateCampaign(new RequestState(code, msg), payload);
    }

    private UpdateCampaign(RequestState state, JSONObject payload) {
        mReqState = state;
        if (payload != null) {
            mVmId = payload.optString("vmid");
            mFileUrlTemplate = payload.optString("file_url");
            mRawItem = payload.optJSONArray("apps");
            if(null != mRawItem) {
                int count = mRawItem.length();
                mItems = new UpdateItem[count];
                for (int i = 0; i < count; i++) {
                    mItems[i] = new UpdateItem(i, this);
                }
            }
        }
    }

    public String getFileUrl(String id) {
        return mFileUrlTemplate.replace("{id}", id);
    }

    public RequestState getState() {
        return mReqState;
    }

    public String getVmId() {
        return mVmId;
    }

    public int getItemCount() {
        return null != mRawItem ? mRawItem.length() : 0;
    }

    JSONObject getRawItem(int index) {
        return mRawItem.optJSONObject(index);
    }

    public UpdateItem getItem(int index) {
        return mItems[index];
    }
}
