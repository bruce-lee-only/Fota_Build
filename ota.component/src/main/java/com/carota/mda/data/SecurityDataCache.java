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

import android.content.Context;
import android.text.TextUtils;

import com.carota.mda.remote.info.SecurityInfo;
import com.carota.mda.remote.info.TokenInfo;
import com.carota.mda.security.SecurityData;
import com.carota.util.DatabaseHolderEx;
import com.momock.util.JsonDatabase;
import com.momock.util.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class SecurityDataCache {

    private static final String SECURITY_CFG = "configure";
    private final JsonDatabase.Collection colData;

    public SecurityDataCache(Context context) {
        //colData = DatabaseHolder.getSecurityData(context);
        colData = DatabaseHolderEx.getSecurityData(context);
        colData.setCachable(true);
    }

    public boolean putData(String fid, TokenInfo data) {
        try {
            JSONObject joData = colData.get(fid);
            if (joData == null) {
                joData = new JSONObject();
            }
            joData.put("m", data.getMd5());
            joData.put("u", data.getFileUrl(data.getMd5()));
            joData.put("z", data.getProp("size", 0L));
            colData.set(fid, joData);
            return true;
        } catch (JSONException je) {
            Logger.error(je);
        }
        return false;
    }

    public SecurityData loadData(String fileId) {
        JSONObject joData = colData.get(fileId);
        if(null != joData) {
            String id = joData.optString("i");
            String type = joData.optString("t");
            String md5 = joData.optString("m");
            String url = joData.optString("u");
            long size = joData.optLong("z");
            SecurityData data = new SecurityData(id, type, md5, url, size);
            if(!md5.isEmpty()) {
                return data;
            }
        }
        return null;
    }

    @Deprecated
    public void reset(String url, String schedule) {
        JSONObject json = colData.get(SECURITY_CFG);
        if(null != json) {
            String cacheUrl = json.optString("url");
            String cacheCampaign = json.optString("schedule");
            if(cacheUrl.equals(url) && cacheCampaign.equals(schedule)) {
                Logger.debug("SDC REUSE");
                return;
            } else {
                Logger.debug("SDC CLEAN");
                cleanCache();
            }
        }
        try {
            colData.set(SECURITY_CFG, new JSONObject()
                    .put("url", url)
                    .put("schedule", schedule));
        } catch (JSONException e) {
            Logger.error(e);
        }
    }

    @Deprecated
    private void cleanCache() {
        for (JsonDatabase.Document doc : colData.list()) {
            colData.set(doc.getId(), null);
        }
    }

    public String getUrl() {
        JSONObject json = colData.get(SECURITY_CFG);
        if(json != null) {
            return json.optString("url");
        }
        return "";
    }

}
