package com.carota.config;

import android.os.SystemClock;
import android.text.TextUtils;

import com.carota.config.data.ConfigInfo;
import com.carota.config.remote.ActionConfig;
import com.carota.config.remote.IActionConfig;
import com.momock.util.EncryptHelper;
import com.momock.util.FileHelper;
import com.momock.util.JsonHelper;
import com.momock.util.Logger;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/*******************************************************************************
 * Copyright (C) 2022-2025 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/
public class ConfigManager {
    private static final String TAG = "Config-Mgr";
    private static final Object sLocker = new Object();
    private static IActionConfig mActionConfig;
    private static AtomicBoolean isProcessing = new AtomicBoolean(false);
    private static final long timeout = 60 * 1000;
    public static String CONFIG_PATH = "";

    private static final String PRE_RAW = "preRaw";
    private static final String SUB_RAW = "subRaw";
    public static final String PREVIOUS = "previous";
    public static final String SUBSEQUENT = "subsequent";
    private static String targetHost;

    public static void init(File configFileDir, String host) {
        synchronized (sLocker) {
            if (mActionConfig == null) {
                mActionConfig = new ActionConfig();
            }

            if (!configFileDir.exists()) {
                FileHelper.mkdir(configFileDir);
            }
            CONFIG_PATH = configFileDir.getAbsolutePath();
            targetHost = host;
        }
    }

    public static void syncPreConfig(String url, String vin, int maxRetry) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                syncConfig(url, vin, null, maxRetry);
            }
        }).start();
    }

    public static void syncConfig(String url, String vin, String scheduleId, int maxRetry) {
        ConfigInfo configInfo = mActionConfig.getConfigInfo(url, vin, scheduleId);
        if (configInfo == null) {
            Logger.error(TAG + "config info parse error");
            return;
        }
        String rawType = "";
        String type = "";
        if (TextUtils.isEmpty(scheduleId)) {
            rawType = PRE_RAW;
            type = PREVIOUS;
        } else {
            rawType = SUB_RAW;
            type = SUBSEQUENT;
        }
        File file = new File(CONFIG_PATH, rawType + "/" + configInfo.getMd5());
        if (file.exists()) {
            String fileMd5 = EncryptHelper.calcFileMd5(file);
            if (configInfo.getMd5().equals(fileMd5)) {
                Logger.debug(TAG + " same config file");
                return;
            }
        } else if (!TextUtils.isEmpty(scheduleId)) {
            cleanConfigDir(SUBSEQUENT);
        }
        cleanConfigDir(rawType);
        File retFile = mActionConfig.downloadConfig(configInfo.getFileUrl(), configInfo.getMd5(), new File(CONFIG_PATH, rawType), maxRetry);
        isProcessing.set(true);
        try {
            Logger.error(TAG + " extracting config file");
            if (TextUtils.isEmpty(scheduleId)) {
                cleanConfigDir(PREVIOUS);
            }
            FileHelper.extractTarGz(retFile, new File(CONFIG_PATH, type).getAbsolutePath());
        } catch (Exception e) {
            cleanConfigDir(rawType);
            Logger.error(TAG + " extract config file error");
            Logger.error(e);
        } finally {
            isProcessing.set(false);
        }
    }

    public static List<ConfigInfo> obtainConfigs(List<String> configNames) {
        if (CONFIG_PATH.isEmpty()) {
            Logger.error(TAG + " config file directory is empty");
            return null;
        }
        long endTime = SystemClock.elapsedRealtime() + timeout;
        while (isProcessing.get()) {
            try {
                if (SystemClock.elapsedRealtime() > endTime) {
                    Logger.debug(TAG + " wait time out");
                    break;
                }
                Logger.debug(TAG + " config file is extracting");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        File preFile = new File(CONFIG_PATH + "/" + PREVIOUS, "meta.json");
        File subFile = new File(CONFIG_PATH + "/" + SUBSEQUENT, "meta.json");
        List<ConfigInfo> list = new ArrayList<>();
        Map<String, String> map = new HashMap<>();
        if (preFile.exists()) {
            try {
                String meta = FileHelper.readText(preFile);
                JSONObject obj = JsonHelper.parseObject(meta);
                for (Iterator<String> it = obj.keys(); it.hasNext(); ) {
                    String key = it.next();
                    map.put(key, obj.optString(key));
                }
            } catch (Exception e) {
                Logger.error(TAG + " previous config meta parse error");
                Logger.error(e);
            }
        }
        if (subFile.exists()) {
            try {
                String subMeta = FileHelper.readText(subFile);
                JSONObject obj2 = JsonHelper.parseObject(subMeta);
                for (Iterator<String> it = obj2.keys(); it.hasNext(); ) {
                    String key = it.next();
                    map.put(key, obj2.optString(key));
                }
            } catch (Exception e) {
                Logger.error(TAG + " subsequent config meta parse error");
                Logger.error(e);
            }
        }
        for (String name : configNames) {
            if (!TextUtils.isEmpty(map.get(name))) {
                ConfigInfo configInfo = new ConfigInfo();
                String url = "http://" + targetHost + "/file?id=" + map.get(name);
                configInfo.setFileUrl(url);
                configInfo.setMd5(name);
                list.add(configInfo);
            }
        }
        return list.size() > 0 ? list : null;
    }

    private static void cleanConfigDir(String type) {
        FileHelper.cleanDir(new File(CONFIG_PATH, type));
    }
}
