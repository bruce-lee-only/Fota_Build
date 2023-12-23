/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.sync.analytics;

import com.carota.mda.remote.info.EventInfo;
import com.carota.sota.remote.ActionSOTA;
import com.carota.sota.remote.IActionSOTA;
import com.carota.sync.base.JsonDataLogger;
import com.momock.util.JsonDatabase;
import com.momock.util.Logger;

import org.json.JSONObject;

public class SoftwareAnalytics extends JsonDataLogger {

    private static final String TYPE_APP = "type_app";

    private IActionSOTA mActionSOTA;
    private String mUrl;

    public SoftwareAnalytics(JsonDatabase.Collection col, String url) {
        super(col);
        mActionSOTA = new ActionSOTA();
        mUrl = url;
    }

    /**
    /data
    {
        "vin":"vehicle id number",
        "state":1,
        "at":192019828,
        "schedule":1,
        "id":"5fa13c9008c41ab4098c73ea"
    }
    */

    public boolean logState(String vin, String cid, int sid, int state, int code) {
        try {
            JSONObject jo = new JSONObject()
                    .put("vin", vin)
                    .put("id", cid)
                    .put("schedule", sid)
                    .put("state", state)
                    .put("at", System.currentTimeMillis())
                    .put("ec", code);
            return recordJsonData(TYPE_APP, vin, jo, false, true);
        } catch (Exception e) {
            Logger.error("[SYNC-SA] State Er");
        }
        return false;
    }

    @Override
    protected boolean send(String id, EventInfo ei) {
        if(ei.TYPE.equals(TYPE_APP)) {
            return mActionSOTA.sendUpgradeReport(mUrl, id, ei);
        }
        Logger.error("[SYNC-SA] TR Er @ Not Implemented");
        return true;
    }
}
