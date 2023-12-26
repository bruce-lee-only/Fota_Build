/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.core.offline;

import android.text.TextUtils;

import com.carota.mda.remote.info.BomInfo;
import com.momock.util.EncryptHelper;
import com.momock.util.JsonHelper;
import com.momock.util.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class OfflineParser {
    private static final String VERSION = "version";
    private static final String CDT = "operation";
    private static final String DATA = "data";
    private static final String IMAGE = "image";

    private static final String ECU = "ecu";
    private static final String ECU_NAME = "name";
    private static final String ECU_FILE = "file";
    private static final String ECU_VER = "ver";
    private static final String ECU_DOMAIN = "domain";
    private static final String ECU_TIMOUT = "timout";

    private static Blueprint parseNomallConfigure(File config, String bom) throws Exception {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(config)));
        String ver = null;
        String cdt = null;

        String ecuName = null;
        String ecuFile = null;
        String ecuVer = null;
        String str;
        boolean enterElec = false;
        while ((str = bufferedReader.readLine()) != null) {
            Logger.debug("[USB] config:%s", str);
            String[] strings = str.replace(" ", "").split("=");
            if (strings.length == 2) {
                switch (strings[0]) {
                    case VERSION:
                        ver = strings[1];
                        break;
                    case CDT:
                        cdt = strings[1];
                        break;
                    case ECU:
                    case ECU_NAME:
                        ecuName = strings[1];
                        break;
                    case ECU_FILE:
                        ecuFile = strings[1];
                        break;
                    case ECU_VER:
                        ecuVer = strings[1];
                        break;
                }
            }
        }
        bufferedReader.close();
        Blueprint blueprint = null;
        if (TextUtils.isEmpty(ecuName)) {
            Logger.error("[USB] Ecu Name is Null");
        } else if (TextUtils.isEmpty(ecuFile)) {
            Logger.error("[USB] Ecu Name is Null");
        } else if (TextUtils.isEmpty(ecuVer)) {
            Logger.error("[USB] Ecu Name is Null");
        } else {
            Module module = new Module(ecuName, 0, 15 * 60 * 1000);
            File file = new File(config.getParent(), ecuFile);
            String md5 = EncryptHelper.calcFileMd5(file);
            Logger.error("[USB] %s@%s", md5, ecuName);
            Module.FileMeta meta = new Module.FileMeta(ecuVer, md5, file);
            module.addFileMeta(meta);
            blueprint = new Blueprint(ver, cdt, getBomCfg(config.getParentFile(), bom));
            blueprint.addModule(module);
        }
        return blueprint;
    }

    private static Blueprint parseConfigure(File file, String bom) throws Exception {
        String config = OfflineDecryptHelper.decryptConfigure(file);
        Logger.debug("[USB] config:%s", config);
        JSONObject obj = JsonHelper.parse(config);
        if (null == obj) {
            return null;
        }

        String ver = obj.optString(VERSION);
        String cdt = obj.optString(CDT);
        JSONArray imageArr = obj.getJSONArray(IMAGE);
        JSONObject dataMap = obj.getJSONObject(DATA);

        Map<String, Module.FileMeta> fileMetaMap = new HashMap<>();
        Iterator<String> itData = dataMap.keys();
        while (itData.hasNext()) {
            String key = itData.next();
            JSONArray jaMeta = dataMap.getJSONArray(key);
            String verName = jaMeta.getString(0);
            String checksum = jaMeta.getString(1);
            fileMetaMap.put(key, new Module.FileMeta(verName, checksum, new File(file.getParent(), checksum)));
        }

        Blueprint bp = new Blueprint(ver, cdt, getBomCfg(file.getParentFile(), bom));
        long defaultTimout = 15 * 60 * 1000;
        for (int i = 0; i < imageArr.length(); i++) {
            JSONObject image = imageArr.optJSONObject(i);
            Module module = new Module(image.getString(ECU_NAME),
                    image.optInt(ECU_DOMAIN), image.optLong(ECU_TIMOUT, defaultTimout));
            JSONObject joModuleFile = image.getJSONObject(ECU_FILE);
            Iterator<String> itModule = joModuleFile.keys();
            while (itModule.hasNext()) {
                String key = itModule.next();
                Module.FileMeta meta = fileMetaMap.get(joModuleFile.getString(key));
                module.addFileMeta(key, meta);
            }
            bp.addModule(module);
        }
        return bp;
    }

    /**
     * file parse
     *
     * @param root              configure File Parent Path
     * @param bom               The BOM information must come from the Service
     * @param configureFileName configure File
     * @param secure            true：from service Encrypt File
     *                          false:from local File and not Encrypt
     * @return
     */
    public static Blueprint parse(File root, String bom, String configureFileName, boolean secure) {
        try {
            File file = new File(root, configureFileName);
            if (file.exists()) {
                if (secure) {
                    return parseConfigure(file, bom);
                } else {
                    return parseNomallConfigure(file, bom);
                }

            }
        } catch (Exception e) {
            Logger.error("[USB] init fail ; err:%s", e.toString());
        }
        return null;
    }

    private static List<BomInfo> getBomCfg(File root, String bom) {
        try {
            if ((!TextUtils.isEmpty(bom))) {
                List<BomInfo> list = new ArrayList<>();
                File bomFile = new File(root, bom);
                String configure = OfflineDecryptHelper.decryptConfigure(bomFile);
                Logger.debug("[USB] Bom config:%s", configure);
                JSONObject obj = JsonHelper.parse(configure);
                if (null != obj) {
                    JSONArray array = obj.getJSONArray("data");
                    for (int i = 0; i < array.length(); i++) {
                        BomInfo bomInfo = BomInfo.fromJson(array.optJSONObject(i));
                        list.add(bomInfo);
                    }
                }
                return list;
            }
        } catch (Exception e) {
            Logger.error(e);
        }
        return null;
    }
}
