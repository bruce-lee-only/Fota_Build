package com.carota.sync.analytics;

import android.text.TextUtils;

import com.carota.build.ParamAnalytics;
import com.carota.mda.remote.ActionAPI;
import com.carota.mda.remote.IActionAPI;
import com.carota.mda.remote.info.EventInfo;
import com.carota.mda.telemetry.FotaAnalytics;
import com.carota.mda.telemetry.FotaState;
import com.carota.sync.base.JsonDataLogger;
import com.momock.util.JsonDatabase;
import com.momock.util.Logger;

import org.json.JSONObject;


public class UpgradeAnalyticsV2 extends JsonDataLogger{
    private final static String TAG = "UpgradeAnalyticsV2";
    private IActionAPI mActionAPI;
    private String mUrl;
    private AnalyticsBridge mBridge;
    private boolean mSupportV2;

    private final static String RECORD_V2_EVENT_TYPE = "vota";
    private final static String RECORD_V2_TYPE = "total_state";
    private final static String RECORD_V2_ECU = "ecu";
    private final static String RECORD_V2_STATE = "state";
    private final static String RECORD_V2_TIME = "time";
    private final static String RECORD_V2_CODE ="code";
    private final static String RECORD_V2_ERROR = "error";

    public UpgradeAnalyticsV2(JsonDatabase.Collection col, ParamAnalytics paramAnal) {
        super(col);
        mActionAPI = new ActionAPI();
        mUrl = paramAnal.getEventV2Url();
        mBridge = new AnalyticsBridge();
        String ver = paramAnal.getEventVersion();
        mSupportV2 = !TextUtils.isEmpty(ver) && ver.equals("v2");
    }

    /**
     *
     * @param usid
     * @param type
     * @param estate
     * @param ecu
     * @param code
     * @param erMsg
     * @return
     *
     * {
     * "$": {
     *         "at": 1590370273451,
     *         "usid": "5ecb1f3fe3489e24543a1279",
     *     },
     *
     * "vota": [
     *         {
     *             "total_state": 1000,
     *             "ecu": "cwc",
     *             "state": 100101,
     *             "time": 1590370273446,
     *             "code":100,
     *             "error":""
     *         }
     *     ]
     * }
     */
    public boolean logAction(String usid, int type, int estate, String ecu, int code, String erMsg)  {
        Logger.debug("FEL ACT @ type = %d, ecu = [%s], code = %d, msg = %s", type, ecu, code, erMsg);
        if(!mSupportV2) {
            Logger.warn(TAG + " project not support v2");
            return false;
        }
        try {
            if(type <100000) {
                type = mBridge.convertV0ToV2State(type);
            }
            if(estate < 100000) {
                estate = mBridge.convertV0ToV2State(estate);
            }
            JSONObject jo = new JSONObject()
                    .put(RECORD_V2_TYPE, type)
                    .put(RECORD_V2_ECU, ecu)
                    .put(RECORD_V2_STATE, estate)
                    .put(RECORD_V2_TIME, System.currentTimeMillis())
                    .put(RECORD_V2_CODE, code)
                    .put(RECORD_V2_ERROR, erMsg);
            return recordJsonData(RECORD_V2_EVENT_TYPE, usid, jo, true, true);
        } catch (Exception e) {
            Logger.error("[SYNC-AA] Act Er");
        }
        return false;
    }


    @Override
    protected boolean onSyncData(JSONObject record) {
        return super.onSyncData(record);
    }

    @Override
    protected boolean send(String id, EventInfo ei) {
        return mActionAPI.sendUpgradeReport(mUrl, id, ei);
    }

    private class AnalyticsBridge extends UpgradeAnalyticsBridge {

        @Override
        int convertState(int state) {
            switch(state) {
                case FotaState.OTA.STATE_RECEIVED:
                    return FotaAnalytics.OTA.STATE_RECEIVED;
                case FotaState.OTA.STATE_DOWNLOADING:
                    return FotaAnalytics.OTA.STATE_DOWNLOADING;
                case FotaState.OTA.STATE_DOWNLOAD_FAILURE:
                    return FotaAnalytics.OTA.STATE_DOWNLOAD_FAIL;
                case FotaState.OTA.STATE_DOWNLOADED:
                    return FotaAnalytics.OTA.STATE_DOWNLOADED;
                case FotaState.OTA.STATE_UPGRADE:
                    return FotaAnalytics.OTA.STATE_UPGRADE;
                case FotaState.OTA.STATE_UPDATE_FAILURE:
                    return FotaAnalytics.OTA.STATE_FAIL;
                case FotaState.OTA.STATE_ROLLBACK_FAILURE:
                    return FotaAnalytics.OTA.STATE_ROLLBACK_FAIL;
                case FotaState.OTA.STATE_ROLLBACK_SUCCESS:
                    return FotaAnalytics.OTA.STATE_ROLLBACK_SUCCESS;
                case FotaState.OTA.STATE_UPGRADE_SUCCESS:
                    return FotaAnalytics.OTA.STATE_SUCCESS;
                default:
                    Logger.warn("unconverted state:%d", state);
            }
            return -1;
        }

        @Override
        int convertV0ToV2State(int state) {
            switch(state) {
                case FotaAnalytics.OTA.STATE_RECEIVED:
                    return FotaState.OTA.STATE_RECEIVED;
                case FotaAnalytics.OTA.STATE_DOWNLOADING:
                    return FotaState.OTA.STATE_DOWNLOADING;
                case FotaAnalytics.OTA.STATE_DOWNLOADED:
                    return FotaState.OTA.STATE_DOWNLOADED;
                case FotaAnalytics.OTA.STATE_UPGRADE:
                    return FotaState.OTA.STATE_UPGRADE;
                case FotaAnalytics.OTA.STATE_SUCCESS:
                    return FotaState.OTA.STATE_UPGRADE_SUCCESS;
                case FotaAnalytics.OTA.STATE_FAIL:
                    return FotaState.OTA.STATE_UPDATE_FAILURE;
                case FotaAnalytics.OTA.STATE_DOWNLOAD_FAIL:
                    return FotaState.OTA.STATE_DOWNLOAD_FAILURE;
                case FotaAnalytics.OTA.STATE_MD5_SUCCESS:
                    return FotaState.OTA.STATE_DOWNLOADED;
                case FotaAnalytics.OTA.STATE_MD5_FAIL:
                    return FotaState.OTA.STATE_DOWNLOAD_FAILURE;
                case FotaAnalytics.OTA.STATE_PKI_SUCCESS:
                    return FotaState.OTA.STATE_UPGRADE;
                case FotaAnalytics.OTA.STATE_PKI_FAIL:
                    return FotaState.OTA.STATE_UPGRADE;
                case FotaAnalytics.OTA.STATE_ROLLBACK_SUCCESS:
                    return FotaState.OTA.STATE_ROLLBACK_SUCCESS;
                case FotaAnalytics.OTA.STATE_ROLLBACK_FAIL:
                    return FotaState.OTA.STATE_ROLLBACK_FAILURE;
                default:
                    Logger.warn("unconverted state:%d", state);
                    return state;
            }
        }

        @Override
        JSONObject convertDataStructure(String usid, String ecu, int state, int code, String msg) {

            return null;
        }
    }
}
