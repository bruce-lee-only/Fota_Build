/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.mda.remote;

import android.os.SystemClock;
import android.text.TextUtils;

import com.carota.component.BuildConfig;
import com.carota.core.ISession;
import com.carota.mda.remote.info.EcuInfo;
import com.carota.mda.remote.info.EventInfo;
import com.carota.mda.remote.info.SecurityInfo;
import com.carota.mda.remote.info.TokenInfo;
import com.carota.util.HttpHelper;
import com.carota.util.LogUtil;
import com.momock.util.JsonHelper;
import com.momock.util.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * OTA server API
 */

public class ActionAPI implements IActionAPI {

    @Override
    public String syncBom(String url, String vin) {
        final String CALL_TAG = LogUtil.TAG_RPC_SVR + "[BOM] ";
        Logger.info(CALL_TAG + "URI : %1s", url);
        try {
            JSONObject root = new JSONObject();
            root.put("vin", vin);
            if(BuildConfig.DEBUG) {
                Logger.debug(CALL_TAG + "BODY : %1s", root.toString());
            }
            HttpHelper.Response response = HttpHelper.doPost(url, null, root);
            Logger.info(CALL_TAG + "RSP : %1d", response.getStatusCode());
            if (200 == response.getStatusCode()) {
                Logger.debug(CALL_TAG + "DATA : %1s", response.getBody());
                return response.getBody();
            }
        } catch (Exception e) {
            Logger.error(e);
        }
        return null;
    }

    /**
     Request body
     "vin": "Vehicle Identification Number",
     "ecus": [
         {
            "name": "ecu id",
            "sv":"software version",
            "hv":"hardware version",
            "sn":"serial number"
        },
     ]
     */
    @Override
    public String connect(String url, String vin, String lang, List<EcuInfo> infos, boolean isFactory) {
        final String CALL_TAG = LogUtil.TAG_RPC_SVR + "[CONN] ";
        Logger.info(CALL_TAG + "URI : %1s", url);
        try {
            JSONObject ecus = new JSONObject();
            for(EcuInfo ei : infos) {
                ecus.put(ei.ID, EcuInfo.toJson(ei));
            }
            JSONObject root = new JSONObject();
            root.put("vin", vin);
            root.put("ecus", ecus);
            root.put("lang",lang);
            if (isFactory)root.put("mode", ISession.MODE_AUTO_UPDATE_FACTORY);
            if(BuildConfig.DEBUG) {
                Logger.debug(CALL_TAG + "BODY : %1s", root.toString());
            }
            HttpHelper.Response response = HttpHelper.doPost(url, null, root);
            Logger.info(CALL_TAG + "RSP : %1d", response.getStatusCode());
            if (200 == response.getStatusCode()) {
                Logger.debug(CALL_TAG + "DATA : %1s", response.getBody());
                return response.getBody();
            }
        } catch (Exception e) {
            Logger.error(e);
        }
        return null;
    }

    // request keys
    private final static String KEY_ROOT = "$";
    private final static String KEY_ID = "usid";
    private final static String KEY_VIN = "vin";
    private final static String KEY_TIME = "at";
    /**
	"$": {
		"at": 1536055217134,
		"usid": "5b55a4ff9ce7f736f85f234c"
	},
	"vupdate": [{
		"ecu": "MCU",
		"state": 4
	}],
    "cust": {"mem":12500}
    **/
    @Override
    public boolean sendUpgradeReport(String url, String usid, EventInfo state) {
        if(TextUtils.isEmpty(usid) || null == state) {
            return false;
        }
        final String CALL_TAG = LogUtil.TAG_RPC_SVR + "[RPT-U] ";
        Logger.info(CALL_TAG + "URI : %1s", url);
        try {
            JSONObject reqBody = new JSONObject()
                    .put(KEY_ROOT, new JSONObject()
                    .put(KEY_TIME, state.getRecordTime())
                    .put(KEY_ID, usid))
                    .put(state.TYPE, state.getPayload());

            if(BuildConfig.DEBUG) {
                Logger.debug(CALL_TAG + "BODY : %1s", reqBody.toString());
            }
            HttpHelper.Response response = HttpHelper.doPost(url, null, reqBody);
            Logger.info(CALL_TAG + "RSP : %1d", response.getStatusCode());
            if (200 == response.getStatusCode()) {
                Logger.debug(CALL_TAG + "DATA : %1s", response.getBody());
                return true;
            }
        } catch (Exception e) {
            Logger.error(e);
        }
        return false;
    }

    /**
     *
     * @param serverUrl
     * @param usid
     * @param state
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
     *             "$$": 1000,
     *             "ecu": "cwc",
     *             "state": 100101,
     *             "time": 1590370273446,
     *             "code":100,
     *             "error":""
     *         }
     *     ]
     * }
     */
    @Override
    public boolean sendUpgradeReportV2(String url, String usid, EventInfo state) {
        if(TextUtils.isEmpty(usid) || null == state) {
            return false;
        }
        final String CALL_TAG = LogUtil.TAG_RPC_SVR + "[RPT-U] ";
        Logger.info(CALL_TAG + "URI : %1s", url);
        try {
            JSONObject reqBody = new JSONObject()
                    .put(KEY_ROOT, new JSONObject()
                            .put(KEY_TIME, state.getRecordTime())
                            .put(KEY_ID, usid))
                    .put(state.TYPE, state.getPayload());

            if(BuildConfig.DEBUG) {
                Logger.debug(CALL_TAG + "BODY : %1s", reqBody.toString());
            }
            HttpHelper.Response response = HttpHelper.doPost(url, null, reqBody);
            Logger.info(CALL_TAG + "RSP : %1d", response.getStatusCode());
            if (200 == response.getStatusCode()) {
                Logger.debug(CALL_TAG + "DATA : %1s", response.getBody());
                return true;
            }
        } catch (Exception e) {
            Logger.error(e);
        }
        return false;
    }


    @Override
    public boolean sendEventReport(String url, String vin, EventInfo state) {
        if(TextUtils.isEmpty(vin) || null == state) {
            return false;
        }
        final String CALL_TAG = LogUtil.TAG_RPC_SVR + "[RPT-EVT] ";
        Logger.info(CALL_TAG + "URI : %1s", url);
        try {
            JSONObject reqBody = new JSONObject()
                    .put(KEY_ROOT, new JSONObject()
                            .put(KEY_TIME, state.getRecordTime())
                            .put(KEY_VIN, vin))
                    .put(state.TYPE, state.getPayload());
            if(BuildConfig.DEBUG) {
                Logger.debug(CALL_TAG + "BODY : %1s", reqBody.toString());
            }
            HttpHelper.Response response = HttpHelper.doPost(url, null, reqBody);
            Logger.info(CALL_TAG + "RSP : %1d", response.getStatusCode());
            if (200 == response.getStatusCode()) {
                Logger.debug(CALL_TAG + "DATA : %1s", response.getBody());
                return true;
            }
        } catch (Exception e) {
            Logger.error(e);
        }
        return false;
    }

    @Override
    public boolean uploadLogFile(String url, String ulid,int len,int index,String md5, byte[] bLog){
        final String CALL_TAG = LogUtil.TAG_RPC_SVR + "[RPT-LOG] ";
        try{
            url = url + "?ulid=" + ulid + "&length=" + len + "&index=" + index + "&md5=" + md5;
            Logger.info(CALL_TAG + "URI : %1s", url);
            HttpHelper.Response resp = HttpHelper.doPost(url, null, bLog);
            Logger.info(CALL_TAG + "RSP : %1d", resp.getStatusCode());
            if(200 == resp.getStatusCode()){
                return true;
            }
            Logger.info(CALL_TAG + "DATA : %1s", resp.getBody());
        }catch (Exception e){
            Logger.error(e);
        }
        return false;
    }

    @Override
    public boolean uploadLogFile(String url, String id, int totalBlockNum, int blockIndex, String md5, InputStream is, String type){
        final String CALL_TAG = LogUtil.TAG_RPC_SVR + "[RPT-LOG] ";
        try{
            Map<String, String> params = new HashMap<>();
            params.put("ulid", id);
            params.put("length", "" + totalBlockNum);
            params.put("index", "" + blockIndex);
            params.put("md5", md5);
            if (!TextUtils.isEmpty(type)) params.put("type", type);
            //url = url + "?ulid=" + id + "&length=" + totalBlockNum + "&index=" + blockIndex + "&md5=" + md5;
            Logger.info(CALL_TAG + "URI : %1s", url);
            HttpHelper.Response resp = HttpHelper.doPost(url, params, is);
            Logger.info(CALL_TAG + "RSP : %1d", resp.getStatusCode());
            if(200 == resp.getStatusCode()){
                return true;
            }
            Logger.info(CALL_TAG + "DATA : %1s", resp.getBody());
        }catch (Exception e){
            Logger.error(e);
        }
        return false;
    }

    /**
     * REQUEST
     {
     "descriptor":"vin/ivi/campaign",
     "data":["src_dm5","dst_md5"],
     "props":{}
     }
     *
     * RESPONSE
     * */
    @Override
    public SecurityInfo querySecurtyInfo(String url, String token, List<String> fileId, long timeout) {
        long targetTime = SystemClock.elapsedRealtime() + timeout;
        final String CALL_TAG = LogUtil.TAG_RPC_SVR + "[SEC] ";
        Logger.info(CALL_TAG + "URI : %1s", url);
        try {
            do {
                JSONObject json = new JSONObject()
                        .put("descriptor", token)
                        .put("data", new JSONArray(fileId))
                        .put("props", new JSONObject());
                if(BuildConfig.DEBUG) {
                    Logger.debug(CALL_TAG + "BODY : %1s", json.toString());
                }
                HttpHelper.Response resp = HttpHelper.doPost(url, null, json);
                Logger.info(CALL_TAG + "RSP : %1d", resp.getStatusCode());
                if (resp.getStatusCode() == 200) {
                    Logger.debug(CALL_TAG + "DATA : %1s", resp.getBody());
                    return SecurityInfo.create(JsonHelper.parseObject(resp.getBody()), fileId.size());
                } else if (resp.getStatusCode() == 202) {
                    Thread.sleep(10 * 1000);
                } else {
                    break;
                }
            } while (targetTime < SystemClock.elapsedRealtime());
        } catch (Exception e) {
            Logger.error(e);
        }
        return null;
    }


    /**
     * REQUEST
     {
     "md5": "b5b4e06dd839c71852d9069310817de8",
     "file_url": "http://127.0.0.1:8080/files/{id}"
     }
     *
     * RESPONSE
     * */
    @Override
    public TokenInfo querySignatureInfo(String url, String cid, String usid, long timeout) {
        long targetTime = SystemClock.elapsedRealtime() + timeout;
        final String CALL_TAG = LogUtil.TAG_RPC_SVR + "[SIG] ";
        Logger.info(CALL_TAG + "URI : %1s", url);
        try {
            do {
                HttpHelper.Response resp = HttpHelper.doGet(url, null);
                Logger.info(CALL_TAG + "RSP : %1d", resp.getStatusCode());
                if (resp.getStatusCode() == 200) {
                    Logger.debug(CALL_TAG + "DATA : %1s", resp.getBody());
                    JSONObject jsonObject = JsonHelper.parseObject(resp.getBody());
                    TokenInfo tokenInfo = new TokenInfo(jsonObject.getJSONObject("data"));
                    return tokenInfo;
                } else if (resp.getStatusCode() == 202) {
                    Thread.sleep(30 * 1000);
                } else {
                    break;
                }
            } while (targetTime < SystemClock.elapsedRealtime());
        } catch (Exception e) {
            Logger.error(e);
        }
        return null;
    }

    /**
     * REQUEST
     {
     "schedule_id":"1234567",
     "vmid":"56789"
     }
     * Return : success connect to server
     * */
    @Override
    public boolean confirmExpired(String url, String vmid, String scheduleId, Map<String, String> extra, AtomicBoolean ret){
        final String CALL_TAG = LogUtil.TAG_RPC_SVR + "[EXPIRE] ";
        Logger.info(CALL_TAG + "URI : %1s", url);
        JSONObject body = null != extra ? new JSONObject(extra) : new JSONObject();
        try {
            body.put("schedule_id", scheduleId);
            body.put("vmid", vmid);
            if(BuildConfig.DEBUG) {
                Logger.debug(CALL_TAG + "BODY : %1s", body.toString());
            }
            HttpHelper.Response resp = HttpHelper.doPost(url, null, body);
            int code = resp.getStatusCode();
            Logger.info(CALL_TAG + "RSP : %1d", code);
            ret.set(200 == code);
            return 200 == code || 408 == code;
        } catch (Exception e) {
            Logger.error(e);
            ret.set(false);
        }
        return false;
    }
}
