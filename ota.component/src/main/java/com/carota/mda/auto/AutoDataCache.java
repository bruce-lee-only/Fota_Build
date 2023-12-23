/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.mda.auto;

import android.content.Context;

import com.carota.mda.data.BomDataCache;
import com.carota.mda.data.UpdateCampaign;
import com.carota.mda.data.UpdateItem;
import com.carota.util.DatabaseHolderEx;
import com.momock.util.JsonDatabase;
import com.momock.util.Logger;

import org.json.JSONException;
import org.json.JSONObject;

public class AutoDataCache {

    private static final String ID_SESSION = "data";
    private static final String ID_DESC = "desc";
    private static final String KEY_RUN = "run";
    private static final String KEY_INTERRUPT = "interrupt";
    private static final String KEY_COUNT = "count";
    private static final String KEY_CID = "cid";
    private static final String KEY_TRIGGER = "trig";

    private final JsonDatabase.Collection colData;
    private BomDataCache mBomDataCache;

    AutoDataCache(Context context) {
        //colData = DatabaseHolder.getSilentData(context);
        colData = DatabaseHolderEx.getDeploySilent(context);
        colData.setCachable(true);
        if(null == colData.get(ID_DESC)) {
            colData.set(ID_DESC, new JSONObject());
        }
        mBomDataCache = new BomDataCache(context);
    }

    void setSession(UpdateCampaign session) {
        if(null == session||!session.check()) {
            colData.set(ID_SESSION, null);
            return;
        }
        try {
            // reset description data if cid is changed
            UpdateItem task = session.getItem(0);
            String cid = task.getProp(UpdateItem.PROP_CID);
            JSONObject joDesc = colData.get(ID_DESC);
            if(!joDesc.optString(KEY_CID).equals(cid)) {
                joDesc.put(KEY_COUNT, 0);
                joDesc.put(KEY_CID, cid);
                colData.set(ID_DESC, joDesc);
            }
            // save session data
            colData.set(ID_SESSION, session.getRawData());
        } catch (JSONException e) {
            Logger.error(e);
        }
    }

    int getRetryCount() {
        return colData.get(ID_DESC).optInt(KEY_COUNT);
    }

    UpdateCampaign getSession() {
        UpdateCampaign us = new UpdateCampaign(colData.get(ID_SESSION), mBomDataCache.getBomInfoList());
        return us.check() ? us : null;
    }

    boolean isRunning() {
        return colData.get(ID_DESC).optBoolean(KEY_RUN);
    }

    public void clear() {
        colData.set(ID_SESSION, null);
    }

    boolean setRunning(boolean run) {
        boolean isTriggered = false;
        try {
            JSONObject joDesc = colData.get(ID_DESC);
            isTriggered = joDesc.optBoolean(KEY_TRIGGER);
            joDesc.put(KEY_RUN, run);
            if(run) {
                joDesc.put(KEY_TRIGGER, true);
            }
            colData.set(ID_DESC, joDesc);
        } catch (JSONException e) {
            Logger.error(e);
        }
        return isTriggered;
    }

    int resetTriggered(boolean countError) {
        int count = -1;
        try {
            if(countError) {
                JSONObject joDesc = colData.get(ID_DESC);
                count = joDesc.optInt(KEY_COUNT);
                if (count < Integer.MAX_VALUE) {
                    count++;
                }
                joDesc.put(KEY_COUNT, count);
                joDesc.put(KEY_TRIGGER, false);
                colData.set(ID_DESC, joDesc);
                Logger.error("reset Trigger");
            } else {
                colData.set(ID_DESC, new JSONObject());
                Logger.error("reset all");
            }
        } catch (JSONException je) {
            Logger.error(je);
        }
        return count;
    }
}
