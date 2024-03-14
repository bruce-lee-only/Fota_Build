package com.carota.config.remote;

import android.text.TextUtils;

import com.carota.component.BuildConfig;
import com.carota.config.data.ConfigInfo;
import com.carota.dm.down.FileDownloader;
import com.carota.dm.down.IDownCallback;
import com.carota.dm.down.IFileDownloader;
import com.carota.dm.file.app.AppFileManager;
import com.carota.util.HttpHelper;
import com.momock.util.JsonHelper;
import com.momock.util.Logger;

import org.json.JSONObject;

import java.io.File;

/*******************************************************************************
 * Copyright (C) 2022-2025 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/
public class ActionConfig implements IActionConfig {
    private final static String CALL_TAG = "CONFIG-EXEC";

    @Override
    public ConfigInfo getConfigInfo(String url, String vin, String scheduleId) {
        JSONObject body = new JSONObject();
        try {
            if (TextUtils.isEmpty(vin)) {
                Logger.error(CALL_TAG + "vin is null");
                return null;
            }
            body.put("vin", vin);
            if (!TextUtils.isEmpty(scheduleId)) {
                body.put("schedule_id", scheduleId);
            }
            if(BuildConfig.DEBUG) {
                Logger.debug(CALL_TAG + "BODY : %1s", body.toString());
            }
            HttpHelper.Response resp = HttpHelper.doPost(url, null, body);
            int code = resp.getStatusCode();
            Logger.info(CALL_TAG + "RSP : %1d", code);
            if (code == 200) {
                if (!TextUtils.isEmpty(resp.getBody())) {
                    return ConfigInfo.fromJson(JsonHelper.parseObject(resp.getBody()));
                }
            }
        } catch (Exception e) {
            Logger.error(e);
        }
        return null;
    }

    public File downloadConfig(String fileUrl, String md5, File fileDir, int maxRetry) {
        AppFileManager mManager = new AppFileManager(fileDir,"cfg");
        File file = new File(fileDir.getAbsolutePath(), md5);
        String url = fileUrl.replace("{id}", md5);
        FileDownloader mDownloader = new FileDownloader(url, md5, mManager, maxRetry, 0, new IDownCallback() {
            @Override
            public void progress(int speed, long length, long fileLength) {
            }
        });
        try {
            if (mDownloader.start() == IFileDownloader.CODE_SUCCESS) {
                Logger.debug(CALL_TAG + " downloaded");
                return file;
            } else {
                if (!mDownloader.isRun()) {
                    Logger.error(CALL_TAG + " STOP");
                } else {
                    Logger.error(CALL_TAG + " ERROR");
                }
            }
        } catch (Exception e) {
            Logger.error(e);
        }
        return null;
    }
}
