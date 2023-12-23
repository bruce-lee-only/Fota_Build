/*
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 */
package com.carota.htmltext.data;

import org.json.JSONObject;

/*{
    "data": {
        "disclaimer": {
            "md5": "6c601c1730e62b273393a379311c6284",
            "size": 2735
        },
        "file_url": "http://192.168.95.151:8084/files/{id}"
    },
    "code": 0,
    "msg": "成功"
}*/
public class HtmlData {
    private JSONObject mRaw;
    private JSONObject mData;
    private JSONObject mDisclaimer;
    private String mFileUrl;

    public HtmlData(JSONObject raw) {
        mRaw = raw;
        mData = mRaw.optJSONObject("data");
        if (mData != null) {
            mDisclaimer = mData.optJSONObject("disclaimer");
            mFileUrl = mData.optString("file_url");
        }
    }

    public JSONObject getRaw() {
        return mRaw;
    }

    public int getCode() {
        return mRaw.optInt("code");
    }

    public String getMsg() {
        return mRaw.optString("msg");
    }

    public String getFileUrl() {
        return mFileUrl;
    }

    public String getMd5() {
        if (mDisclaimer != null) {
            return mDisclaimer.optString("md5");
        }
        return "";
    }

    public int getSize() {
        if (mDisclaimer != null) {
            return mDisclaimer.optInt("size");
        }
        return 0;
    }
}
