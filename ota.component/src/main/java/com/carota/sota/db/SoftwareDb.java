/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/
package com.carota.sota.db;

import com.momock.util.JsonDatabase;
import com.momock.util.Logger;

import org.json.JSONObject;

import java.util.List;

public class SoftwareDb {
    private JsonDatabase.Collection mCollection;

    public SoftwareDb(JsonDatabase.Collection collection) {
        mCollection = collection;
    }

    public void setCampaignState(String packageName, int installCount, String id) {
        try {
            JSONObject object = new JSONObject();
            object.put("pn", packageName);
            object.put("id", id);
            object.put("insRetry", installCount);
            mCollection.set(packageName, object);
        } catch (Exception e) {
            Logger.error("saveAppInfoToDb error " + e.getMessage());
        }
    }

    public void deleteCampaignState(String packageName) {
        mCollection.set(packageName, null);
    }

    public String findIdByName(String packageName) {
        JSONObject data = mCollection.get(packageName);
        return null != data ? data.optString("id") : null;
    }

    public int findInsRetryCountByName(String packageName) {
        JSONObject data = mCollection.get(packageName);
        return null != data ? data.optInt("insRetry") : 0;
    }

    public void saveSelfInfo(String vin, String packageName, int versionCode, String versionName, int schedule, String id) {
        try {
            JSONObject object = new JSONObject();
            object.put("vin", vin);
            object.put("packageName", packageName);
            object.put("versionCode", versionCode);
            object.put("versionName", versionName);
            object.put("schedule", schedule);
            object.put("id", id);
            mCollection.set("self", object);
            Logger.debug("saveSelfInfo packageName = " + packageName);
        } catch (Exception e) {
            Logger.error("saveSelfInfo error " + e.getMessage());
        }
    }

    public JSONObject findSelfInfo() {
        return mCollection.get("self");
    }

    public void deleteSelfInfo() {
        mCollection.set("self", null);
    }
}
