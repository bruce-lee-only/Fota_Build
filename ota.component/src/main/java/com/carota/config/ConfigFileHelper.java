/*
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 */
package com.carota.config;

import android.content.Context;

import com.carota.CarotaClient;
import com.carota.core.VehicleCondition;
import com.carota.core.remote.IActionMDA;
import com.momock.util.FileHelper;
import com.momock.util.JsonHelper;
import com.momock.util.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ConfigFileHelper {
    public static final String CONFIG_PRE_CON_OTA = "ota";
    public static final String CONFIG_PRE_CON_NOTIFY = "notify";
    public static final String CONFIG_PRE_CON_INTERRUPT = "interrupt";
    public static final String CONFIG_PRE_CON_APPOINTMENT = "appointment";
    public static final String CONFIG_CC_TEXT = "cc_text";

    /**
     *
     * @param context Context
     * @param type 参考 CONFIG_PRE_CON_OTA,CONFIG_PRE_CON_NOTIFY,CONFIG_PRE_CON_INTERRUPT,CONFIG_PRE_CON_APPOINTMENT
     * @return List<VehicleCondition.Precondition>
     */
    public static synchronized List<VehicleCondition.Precondition> getPreconditionConfig(Context context, String type) {
        try {
            List<String> list = new ArrayList<>();
            list.add(IActionMDA.CONFIG_PRE_CON);
            Map<String, String> fileMap = CarotaClient.getConfigFiles(context.getFilesDir().getAbsolutePath(), list);
            if (fileMap == null || !fileMap.containsKey(IActionMDA.CONFIG_PRE_CON)) {
                Logger.error("getPreconditionConfig error");
                return null;
            }
            File file = new File(fileMap.get(IActionMDA.CONFIG_PRE_CON));
            JSONObject object = JsonHelper.parseObject(FileHelper.readText(file));
            Logger.info("getPreconditionConfig object = " + object);
            JSONArray jsonArray = object.getJSONArray(type);
            if (jsonArray.length() > 0) {
                List<VehicleCondition.Precondition> preconditionList = new ArrayList<>();
                for (int i = 0; i < jsonArray.length(); i++) {
                    preconditionList.add(VehicleCondition.Precondition.parse(jsonArray.getString(i)));
                }
                return preconditionList;
            }
        } catch (Exception e) {
            Logger.error(e);
        }
        return null;
    }

    /**
     *
     * @param context Context
     * @return Map<String, String>
     */
    public static synchronized Map<String, String> getPreconditionTextConfig(Context context) {
        try {
            List<String> list = new ArrayList<>();
            list.add(IActionMDA.CONFIG_CC_TEXT);
            Map<String, String> fileMap = CarotaClient.getConfigFiles(context.getFilesDir().getAbsolutePath(), list);
            if (fileMap == null || !fileMap.containsKey(IActionMDA.CONFIG_CC_TEXT)) {
                Logger.error("@CfgFile get cctxt error");
                return null;
            }
            File file = new File(Objects.requireNonNull(fileMap.get(IActionMDA.CONFIG_CC_TEXT)));
            JSONObject object = JsonHelper.parseObject(FileHelper.readText(file));
            Logger.info("@CfgFile getPreconditionConfig object = " + object);
            JSONObject txtObj = object.optJSONObject(CONFIG_CC_TEXT);
            if (txtObj != null) {
                Map<String, String> preconditionMap = new HashMap<>();
                for (Iterator<String> it = txtObj.keys(); it.hasNext(); ) {
                    String key = it.next();
                    String value = txtObj.optString(key);
                    if (!value.isEmpty()) {
                        preconditionMap.put(key, value);
                    }
                }
                return preconditionMap;
            }
        } catch (Exception e) {
            Logger.error(e);
        }
        return null;
    }
}
