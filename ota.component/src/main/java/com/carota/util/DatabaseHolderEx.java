/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.util;

import android.content.Context;

import com.carota.build.ParamRAS;
import com.momock.util.JsonDatabase;
import com.momock.util.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class DatabaseHolderEx {
    private static final String DATABASE_FWM = "fwm.data";
    private static final String COL_CACHE = "cache";
    private static final String COL_DEPLOY_STATUS = "deploy_status_";
    private static final String COL_EVENT_PREFIX = "event_";
    private static final String COL_SECURITY = "security";
    private static final String COL_DEPLOY_SILENT = "deploy_silent";
    private static final String COL_STATE_V2_FOTA = "state_v2_fota";
    private static final String COL_BILL_OF_MATERIAL = "bill_of_material";

    private static final String DATABASE_MEMO = "memo.data";
    private static final String COL_EVENT_APP = "event_app";
    private static final String COL_LOG_APP = "log_app";
    private static final String COL_EVENT_VEHICLE = "event_vehicle";

    private static final String DATABASE_SWM = "swm.data";
    private static final String COL_DATA_SOTA = "data_sota";
    private static final String COL_STATE_SOTA = "state_sota";

    private static final String DATABASE_DTC = "dtc.data";
    private static final String COL_LOG_SYS = "log_sys";

    private static final Map<String, JsonDatabase> sDatabasePools = new HashMap<>();

    private static JsonDatabase.Collection get(Context context, String db,
                                               String colName, boolean fifoCache) {
        synchronized (DatabaseHolderEx.class) {
            if(!sDatabasePools.containsKey(db)) {
                sDatabasePools.put(db, JsonDatabase.get(context, db));
            }
        }
        JsonDatabase jdb = sDatabasePools.get(db);
        return fifoCache ? jdb.getCacheCollection(colName) : jdb.getCollection(colName);
    }

    /**
     * Main OTA Data
     */
    public static JsonDatabase.Collection getAppCache(Context context) {
        return get(context, DATABASE_FWM, COL_CACHE, false);
    }

    public static JsonDatabase.Collection getDeployStatus(Context context, String name) {
        return get(context, DATABASE_FWM, COL_DEPLOY_STATUS.concat(name), false);
    }

    public static JsonDatabase.Collection getDeploySilent(Context context) {
        return get(context, DATABASE_FWM, COL_DEPLOY_SILENT, false);
    }

    public static JsonDatabase.Collection getSecurityData(Context context) {
        return get(context, DATABASE_FWM, COL_SECURITY, false);
    }

    public static JsonDatabase.Collection getSlaveEvent(Context context, String slaveName) {
        return get(context, DATABASE_FWM, COL_EVENT_PREFIX + slaveName, true);
    }

    public static JsonDatabase.Collection getFotaV2State(Context context) {
        return get(context, DATABASE_FWM, COL_STATE_V2_FOTA, true);
    }

    /**
     * Synchronize bill of material
     */
    public static JsonDatabase.Collection getBom(Context context) {
        return get(context, DATABASE_FWM, COL_BILL_OF_MATERIAL, true);
    }

    /**
     * APP Logger
     */
    public static JsonDatabase.Collection getAppEvent(Context context) {
        return get(context, DATABASE_MEMO, COL_EVENT_APP, true);
    }

    public static JsonDatabase.Collection getAppLog(Context context) {
        return get(context, DATABASE_MEMO, COL_LOG_APP, false);
    }

    public static JsonDatabase.Collection getVehicleEvent(Context context) {
        return get(context, DATABASE_MEMO, COL_EVENT_VEHICLE, true);
    }
    /**
     * SWM(Software Manager) Data
     */
    public static JsonDatabase.Collection getSotaData(Context context) {
        return get(context, DATABASE_SWM, COL_DATA_SOTA, false);
    }

    public static JsonDatabase.Collection getSotaState(Context context) {
        return get(context, DATABASE_SWM, COL_STATE_SOTA, true);
    }

    /**
     * DTC(Diagnosis & Telemetry Collector)
     */
    public static JsonDatabase.Collection getSysLog(Context context) {
        return get(context, DATABASE_DTC, COL_LOG_SYS, false);
    }

//    private static void copyCollection(JsonDatabase.Collection src, JsonDatabase.Collection dst) {
//        for(JsonDatabase.Document doc : src.list()) {
//            dst.set(doc.getId(), doc.getData());
//        }
//    }
//
//    public static void upgrade(Context ctx) {
//        if(DatabaseHolder.isExist(ctx)) {
//            upgradeFromA2B(ctx);
//            DatabaseHolder.delete(ctx);
//        }
//    }
//
//    private static void upgradeFromA2B(Context ctx) {
//        Logger.debug("DB Upgrade[A2B] START");
//        /**** MAIN ****/
//        // copy app cache
//        copyCollection(DatabaseHolder.getCache(ctx), DatabaseHolderEx.getAppCache(ctx));
//
//        // copy deploy status
//        JsonDatabase.Collection mainCol = DatabaseHolder.getSlaveDA(ctx, "main_tab");
//        JSONObject joRoot = mainCol.get("tab");
//        JSONArray joChild = null != joRoot ? joRoot.optJSONArray("child") : null;
//        for (int i = 0; null != joChild && i < joChild.length(); i++) {
//            String name = joChild.optString(i);
//            copyCollection(DatabaseHolder.getSlaveDA(ctx, name), DatabaseHolderEx.getDeployStatus(ctx, name));
//        }
//        copyCollection(mainCol, DatabaseHolderEx.getDeployStatus(ctx, "main_tab"));
//
//        // copy deploy silent
//        copyCollection(DatabaseHolder.getSilentData(ctx), DatabaseHolderEx.getDeploySilent(ctx));
//
//        // copy security data
//        copyCollection(DatabaseHolder.getSecurityData(ctx), DatabaseHolderEx.getSecurityData(ctx));
//
//        // copy slave event
//        for(ParamRAS.Info info : ConfigHelper.get(ctx).get(ParamRAS.class).listInfo()) {
//            copyCollection(DatabaseHolder.getSlaveEvent(ctx, info.getId()),
//                    DatabaseHolderEx.getSlaveEvent(ctx, info.getId()));
//        }
//
//        // copy upgrade event and app event
//        copyCollection(DatabaseHolder.getMasterEvent(ctx), DatabaseHolderEx.getFotaState(ctx));
//
//        /**** APP ****/
//        // del APP log cache
////        DatabaseHolder.getTelemetry(ctx).clear();
////        DatabaseHolder.getRemote(ctx).clear();
////        DatabaseHolder.getAnalytics(ctx).clear();
//        copyCollection(DatabaseHolder.getBuryingPointData(ctx),DatabaseHolderEx.getAppEvent(ctx));
//        /**** SWM ****/
//
//        /**** DTC ****/
//        // del SYS log cache
////        DatabaseHolder.getFilterLog(ctx).clear();
//        Logger.debug("DB Upgrade[A2B] STOP");
//    }
}
