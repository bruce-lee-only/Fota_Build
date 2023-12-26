/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/
package com.carota.sota.remote;

import android.text.TextUtils;

import com.carota.component.BuildConfig;
import com.carota.mda.remote.info.EventInfo;
import com.carota.sota.store.UpdateCampaign;
import com.carota.util.HttpHelper;
import com.carota.util.LogUtil;
import com.momock.util.Logger;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ActionSOTA implements IActionSOTA {
    /*
        /apps
        {
            "vin":"vehicle id number",
            "brand":"vehicle brand",
            "model":"vehicle model",
            "extra":{
                "key1":"val1",
            }
        }
        */
    @Override
    public UpdateCampaign queryAppList(String url, String vin, String brand, String model, Map<String, String> extra) {
        final String CALL_TAG = LogUtil.TAG_RPC_SOTA + "[RPT-SOTA-QUERY] ";
        try{
            Logger.info(CALL_TAG + "URI : %1s", url);
            HashMap<String, String> headers = new HashMap<>();
            headers.put("accept","application/json");
            JSONObject root = new JSONObject();
            root.put("vin", vin);
            if (!TextUtils.isEmpty(brand)) {
                root.put("brand", brand);
            }
            if (!TextUtils.isEmpty(model)) {
                root.put("model", model);
            }
            if (extra != null && extra.size() > 0) {
                JSONObject object = new JSONObject();
                for (Map.Entry<String, String> entry : extra.entrySet()) {
                    object.put(entry.getKey(), entry.getValue());
                }
                root.put("extra", object);
            }
            if (BuildConfig.DEBUG) {
                Logger.debug(CALL_TAG + "BODY : %1s", root.toString());
            }
            HttpHelper.Response resp = HttpHelper.doPost(url, null, headers, root);
            Logger.info(CALL_TAG + "RSP : %1d", resp.getStatusCode());
            if (BuildConfig.DEBUG) {
                Logger.debug(CALL_TAG + "DATA : %1s", resp.getBody());
            }
            if (resp.getStatusCode() == RESPONSE_ERROR) {
                return null;
            }
            return UpdateCampaign.parseCampaign(resp.getBody());
        } catch (Exception e){
            Logger.error(CALL_TAG + e);
        }
        Logger.info(CALL_TAG + "return null");
        return null;
    }

    /*
    /data
    {
        "vin":"vehicle id number",
        "state":1,
        "at":192019828,
        "schedule":1,
        "id":"5fa13c9008c41ab4098c73ea"
    }
    */

    public boolean sendUpgradeReport(String url, String vin, EventInfo ei) {
        final String CALL_TAG = LogUtil.TAG_RPC_SOTA + "[RPT-SOTA-RECORD] ";
        try{
            Logger.info(CALL_TAG + "URI : %1s", url);
            HttpHelper.Response resp = HttpHelper.doPost(url, null, ei.getPayload());
            Logger.info(CALL_TAG + "RSP : %1d", resp.getStatusCode());
            Logger.info(CALL_TAG + "DATA : %1s", resp.getBody());
            if(RESPONSE_OK == resp.getStatusCode()){
                return true;
            }
        }catch (Exception e){
            Logger.error(e);
        }
        return false;
    }
}
