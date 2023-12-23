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

import java.util.ArrayList;
import java.util.List;

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

public class AppData {

    final UpdateCampaign campaign;
    private final List<Integer> mValidItemIndexList;

    AppData(UpdateCampaign uc) {
        campaign = uc;
        mValidItemIndexList = new ArrayList<>();
    }

    void setValidItem(UpdateItem item) {
        mValidItemIndexList.add(item.getIndex());
    }

    public int getCode() {
        return campaign.getState().code;
    }

    public String getMsg() {
        return campaign.getState().message;
    }

    public String getVehicleModuleID() {
        return campaign.getVmId();
    }

    public String getFileUrl(String id) {
        return campaign.getFileUrl(id);
    }

    public AppInfo getAppInfo(int index) {
        return campaign.getItem(mValidItemIndexList.get(index));
    }

    public int getAppInfoCount() {
        return mValidItemIndexList.size();
    }

    /*
    @Deprecated
    public List<AppInfo> getAppInfoList() {
        List<AppInfo> ret = new ArrayList<>();
        for(int i = 0; i < getAppInfoCount(); i++) {
            ret.add(getAppInfo(i));
        }
        return ret;
    }
     */
}
