package com.carota.sync.analytics;

import org.json.JSONObject;

public abstract class UpgradeAnalyticsBridge {
    abstract int convertState(int state);

    abstract int convertV0ToV2State(int state);

    abstract JSONObject convertDataStructure(String usid, String ecu, int state, int code, String msg);

}
