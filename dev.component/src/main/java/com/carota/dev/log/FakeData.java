package com.carota.dev.log;

import org.json.JSONArray;
import org.json.JSONObject;

public class FakeData {

    public static JSONObject createResponse(String vin) throws Exception{
        JSONArray joTask = new JSONArray();
        JSONObject resp = new JSONObject()
                .put("vin", vin)
                .put("usid", "5c21fccf1f23170e67b24d84")
                .put("file_url", "http://api.carota.ai/files/{id}")
                .put("ecus", joTask);

        joTask.put(new JSONObject()
                .put("id", "1008")
                .put("name", "ivi")
                .put("sv", "sw-dummy-v1")
                .put("tv", "sw_dummy-v2")
                .put("smd5", "dmy-src")
                .put("dmd5", "dmy-dst")
                .put("rn", "升级包下载DEMO")
                .put("s_size", "2616048")
                .put("d_size", "7162928")
        );
        return resp;
    }
}
