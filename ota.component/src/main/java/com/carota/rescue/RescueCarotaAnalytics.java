package com.carota.rescue;

import android.content.Context;

import com.carota.CarotaClient;
import com.carota.core.ISession;
import com.carota.sync.DataSyncManager;
import com.carota.sync.analytics.AppAnalytics;
import com.carota.sync.analytics.UpgradeAnalyticsV2;

public class RescueCarotaAnalytics {

    private static AppAnalytics sAppAnalytics = null;
    private static UpgradeAnalyticsV2 sUpgradeAnalytics2;

    public static synchronized void init(Context context) {
        if(null == sAppAnalytics) {
            sAppAnalytics = DataSyncManager.get(context).getSync(AppAnalytics.class);
            sUpgradeAnalytics2 = DataSyncManager.get(context).getSync(UpgradeAnalyticsV2.class);
        }
    }

    public static boolean setUpgradeBuryingPoint(int type, String describe, long time){
        if(null == sAppAnalytics) {
            throw new RuntimeException("Need Init First @ CA");
        }
        ISession s = RescueCarotaClient.getClientSession();
        if(null != s) {
            return sAppAnalytics.logAction(s.getUSID(), type, describe, time);
        }
        return false;
    }

    public static boolean setUpgradeAnalytics(int totState, int state, String ecu, int code, String erMsg) {
        if(null == sUpgradeAnalytics2) {
            throw new RuntimeException("Need Init First @ CA");
        }
        ISession s = CarotaClient.getClientSession();
        if(null != s) {
            return sUpgradeAnalytics2.logAction(s.getUSID(), totState, state, ecu, code, erMsg);
        }
        return false;
    }

    public static void syncUpgradeAnalytics() {
        if(null != sUpgradeAnalytics2)
            sUpgradeAnalytics2.syncData();
        if(null != sAppAnalytics)
            sAppAnalytics.syncData();
    }
}
