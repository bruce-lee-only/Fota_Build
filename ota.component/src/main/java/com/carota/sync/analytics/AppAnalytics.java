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

import com.carota.mda.remote.ActionAPI;
import com.carota.mda.remote.IActionAPI;
import com.carota.mda.remote.info.EventInfo;
import com.carota.sync.base.JsonDataLogger;
import com.momock.util.JsonDatabase;
import com.momock.util.Logger;

import org.json.JSONObject;

public class AppAnalytics extends JsonDataLogger {

    private IActionAPI mActionAPI;
    private String mUrl;

    public AppAnalytics(JsonDatabase.Collection col, String url) {
        super(col);
        mActionAPI = new ActionAPI();
        mUrl = url;
    }

    /**
     "$": {
        "usid": "5f02d259238771438c3aed3b",
        "at": 1594020554316,
     }，
     "vaction": [
        {
         "id": 10101,
         "at": 1594020554276,
         "msg": "Set Timer 2020.05.20"
        }
     ]
    */

    public boolean logAction(String usid, int type, String msg,long time)  {
        Logger.debug("FEL ACT @ %d [%s]", type, msg);
        try {
            JSONObject jo = new JSONObject()
                    .put("id", type)
                    .put("at", time)
                    .put("msg", msg);
            return recordJsonData("vaction", usid, jo, true, true);
        } catch (Exception e) {
            Logger.error("[SYNC-AA] Act Er");
        }
        return false;
    }

    @Override
    protected boolean send(String id, EventInfo ei) {
        return mActionAPI.sendUpgradeReport(mUrl, id, ei);
    }

    /**
     "$": {
     "usid": "5f02d259238771438c3aed3b",
     "at": 1594020554316,
     }，
     "eaction": [
         {
             "at": 1676963828,
             "upgrade_type ": 0,
             "EIC_system": 0,
             "schedule_id": "1234",
             "event_code": 0,
             "msg": "",
             "result": 0
         }
     ]
     */
    public boolean logAction(String usid, long at, int upgradeType,
                             int eventCode, String msg, int result, String scheduleId, int eicSystem) {
        Logger.debug("FEL EACT @ %d [%d %d %d %s]", upgradeType, eventCode,result,eicSystem,msg);
        try {
            JSONObject jo = new JSONObject()
                    .put("at", at)
                    .put("upgrade_type", upgradeType)
                    .put("EIC_system", eicSystem)
                    .put("schedule_id", scheduleId)
                    .put("event_code", eventCode)
                    .put("result", result)
                    .put("msg", msg);
            return recordJsonData("eaction", usid, jo, true, true);
        } catch (Exception e) {
            Logger.error("[SYNC-AA] Act Er");
        }
        return false;
    }
}
