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

import com.momock.util.JsonDatabase;
import com.momock.util.Logger;

import java.util.Arrays;
import java.util.List;


public class DatabaseHolder {
    private static final String DATABASE = "ota.data";
    private static final String COL_ANALYTICS = "analytics";
    private static final String COL_CACHE = "cache";
    private static final String COL_REMOTE = "remote";
    private static final String COL_SLAVE = "slave_";
    private static final String COL_EVENT_PREFIX = "event_";
    private static final String COL_TELEMETRY = "telemetry";
    private static final String COL_SECURITY = "security";
    private static final String COL_SILENT = "silent";
    private static final String COL_FILTER_LOG = "filter_log";
    private static final String COL_BURYING_POINT = "notify";

    private static JsonDatabase sDatabase = null;

    private static JsonDatabase.Collection get(Context context, String colName) {
        synchronized (DATABASE) {
            if (null == sDatabase) {
                sDatabase = JsonDatabase.get(context, DATABASE);
            }
        }
        return sDatabase.getCollection(colName);
    }

    public static JsonDatabase.Collection getAnalytics(Context context) {
        return get(context, COL_ANALYTICS);
    }

    public static JsonDatabase.Collection getCache(Context context) {
        return get(context, COL_CACHE);
    }

    public static JsonDatabase.Collection getRemote(Context context) {
        return get(context, COL_REMOTE);
    }

    public static JsonDatabase.Collection getSlaveDA(Context context, String name) {
        return get(context, COL_SLAVE.concat(name));
    }

    public static JsonDatabase.Collection getSlaveEvent(Context context,String slaveName) {
        return get(context, COL_EVENT_PREFIX + slaveName);
    }

    public static JsonDatabase.Collection getMasterEvent(Context context) {
        return get(context, COL_EVENT_PREFIX + "master");
    }

    public static JsonDatabase.Collection getTelemetry(Context context) {
        return get(context, COL_TELEMETRY);
    }

    public static JsonDatabase.Collection getSecurityData(Context context) {
        return get(context,COL_SECURITY);
    }
    public static JsonDatabase.Collection getSilentData(Context context) {
        return get(context,COL_SILENT);
    }

    public static JsonDatabase.Collection getBuryingPointData(Context context) {
        return get(context,COL_BURYING_POINT);
    }

    public static JsonDatabase.Collection getFilterLog(Context context) {
        return get(context,COL_FILTER_LOG);
    }

    public static boolean isExist(Context context) {
        // List<String> dbs = Arrays.asList(context.databaseList());
        // Logger.debug("DBH = " + dbs.toString());
        return Arrays.asList(context.databaseList()).contains(DatabaseHolder.DATABASE);
    }

    public static void delete(Context context) {
        synchronized (DATABASE) {
            if (null != sDatabase) {
                sDatabase.forceClose();
                context.deleteDatabase(DATABASE);
                sDatabase = null;
            }
        }
    }
}
