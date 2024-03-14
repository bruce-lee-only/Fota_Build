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

import android.text.TextUtils;

import com.carota.build.ParamRoute;
import com.carota.mda.remote.ActionAPI;
import com.carota.mda.remote.ActionSDA;
import com.carota.mda.remote.IActionAPI;
import com.carota.mda.remote.IActionSDA;
import com.carota.mda.remote.info.EventInfo;
import com.carota.sync.base.JsonDataLogger;
import com.momock.util.JsonDatabase;
import com.momock.util.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class VehicleAnalytics extends JsonDataLogger {

    private IActionAPI mActionAPI;
    private IActionSDA mActionSDA;
    private ParamRoute mRoute;
    private String mUrl;
    private String mVin;

    public VehicleAnalytics(JsonDatabase.Collection col, String url, ParamRoute route) {
        super(col);
        mRoute = route;
        mActionSDA = new ActionSDA();
        mActionAPI = new ActionAPI();
        mUrl = url;
        mVin = null;
    }

    /**
     "$": {
        "at": 1536055217134,
        "vin": "5b55a4ff9ce7f736f85f234c"
     },
     "cust": [
        {
         "ecu":"vcu",
         "msg":["msg1","msg2"]
        }
     ]
     */

    private boolean logEvent(String vin, String ecu, List<String> data) {
        try {
            JSONObject jo = new JSONObject()
                    .put("ecu", ecu)
                    .put("msg", new JSONArray(data));
            return recordJsonData("cust", vin, jo, true, false);
        } catch (Exception e) {
            Logger.error(e);
        }
        return false;
    }

    private void syncEventFromECU(String vin, ParamRoute allRoute, String type) {
        for (ParamRoute.Info i : allRoute.listEcuInfo()) {
            String host = ParamRoute.getEcuHost(i);
            if (TextUtils.isEmpty(host)) host = allRoute.getSubHost();
            if (TextUtils.isEmpty(host)) {
                Logger.error("[SYNC-VA] Poll Er @ Host");
                continue;
            }

            IActionSDA.IOnUploadEvent callback = new IActionSDA.IOnUploadEvent() {

                @Override
                public boolean send(List<String> data) {
                    return logEvent(vin, i.ID, data);
                }
            };
            try {
                mActionSDA.uploadEvent(host, type, 10, callback);
            } catch (Exception e) {
                Logger.error("[SYNC-VA] Poll Er @ API", e);
            }
        }
    }

    public void setVIN(String vin) {
        mVin = vin;
        syncData();
    }

    @Override
    protected boolean onSyncPrepare() {
        if (TextUtils.isEmpty(mVin)) {
            Logger.error("[SYNC-VA] Prepare Er @ Parameter");
            return false;
        }
        syncEventFromECU(mVin, mRoute, null);
        return true;
    }

    @Override
    protected boolean send(String id, EventInfo ei) {
        return mActionAPI.sendEventReport(mUrl, id, ei);
    }
}
