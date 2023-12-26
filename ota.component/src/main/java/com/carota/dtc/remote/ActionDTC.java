/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.dtc.remote;

import com.carota.component.BuildConfig;
import com.carota.dtc.log.data.Instruction;
import com.carota.mda.remote.info.EcuInfo;
import com.carota.util.HttpHelper;
import com.carota.util.LogUtil;
import com.momock.util.JsonHelper;
import com.momock.util.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

/**
 * filter log server API
 */

public class ActionDTC implements IActionDTC {

    @Override
    public int uploadFilterLogFile(String url, String token, int length, int index, String md5, String fullMd5, String sc, InputStream is) {
        final String CALL_TAG = LogUtil.TAG_RPC_DTC + "[RPT-FILTER-LOG] ";
        try{
            Logger.info(CALL_TAG + "URI : %1s", url);
            HashMap<String, String> headers = new HashMap<>();
            headers.put("id",token);
            headers.put("index",index + "-" + length);
            headers.put("md5",md5);
            headers.put("fullMd5",fullMd5);
            headers.put("securityCode",sc);
            headers.put("accept", "application/json");
            Logger.info(CALL_TAG + "headers : %1s", headers.toString());
            HttpHelper.Response resp = HttpHelper.doPost(url, null, headers, is);
            Logger.info(CALL_TAG + "RSP : %1d", resp.getStatusCode());
            Logger.info(CALL_TAG + "DATA : %1s", resp.getBody());
            if(200 == resp.getStatusCode()){
                return new Instruction(JsonHelper.parseObject(resp.getBody())).getCode();
            }
        }catch (Exception e){
            Logger.error(CALL_TAG + e);
        }
        return -1;
    }

    /**
     Request body
     {
        "id": "VIN",
         "caps": [],
         "ecus": [
             {
                 "name": "hu",
                 "hv": "hardware version",
                 "sv": "software version"
             }
         ]
     }
     */
    @Override
    public String queryDtcTask(String url, String vin, List<String> caps, List<EcuInfo> ecuInfoList) {
        final String CALL_TAG = LogUtil.TAG_RPC_DTC + "[RPT-FILTER-TASK] ";
        try{
            Logger.info(CALL_TAG + "URI : %1s", url);
            HashMap<String, String> headers = new HashMap<>();
            headers.put("accept","application/json");
            JSONArray ecus = new JSONArray();
            if (null != ecuInfoList) {
                for(EcuInfo ei : ecuInfoList) {
                    JSONObject ecu = new JSONObject();
                    ecu.put("name", ei.getName());
                    ecu.put("hv", ei.getHardwareVer());
                    ecu.put("sv", ei.getSoftwareVer());
                    ecus.put(ecu);
                }
            }
            // for rollback
            if (ecus.length() == 0) {
                JSONObject ivi = new JSONObject();
                ivi.put("name", "empty");
                ivi.put("hv", "dummy");
                ivi.put("sv", "dummy");
                ecus.put(ivi);
            }

            JSONObject root = new JSONObject();
            root.put("vin", vin);
            JSONArray capsArray = new JSONArray(caps);
            root.put("caps", capsArray);
            root.put("ecus", ecus);
            if(BuildConfig.DEBUG) {
                Logger.debug(CALL_TAG + "BODY : %1s", root.toString());
            }
            HttpHelper.Response resp = HttpHelper.doPost(url, null, headers,root);
            Logger.info(CALL_TAG + "RSP : %1d", resp.getStatusCode());
            Logger.info(CALL_TAG + "DATA : %1s", resp.getBody());
            if(200 == resp.getStatusCode()){
                return resp.getBody();
            }
        }catch (Exception e){
            Logger.error(CALL_TAG + e);
        }
        return null;
    }
}
