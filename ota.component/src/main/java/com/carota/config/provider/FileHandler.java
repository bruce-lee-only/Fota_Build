/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.config.provider;

import android.content.Context;

import com.carota.config.ConfigManager;
import com.carota.svr.HttpResp;
import com.carota.svr.SimpleHandler;
import com.momock.util.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import fi.iki.elonen.HttpStatusCode;

public class FileHandler extends SimpleHandler {
    private static final String TAG = "CM-SEARCH";

    protected final Context mContext;

    public FileHandler(Context context) {
        super();
        mContext = context;
    }

    @Override
    public HttpResp get(String path, Map<String, List<String>> params, Object extra) {
        Logger.info(TAG + "[file] : FileHandler");
        try {
            Logger.info(TAG + "[search] : FileHandler - " + params);
            if (params.isEmpty() || !params.containsKey("id")) {
                return HttpResp.newInstance(HttpStatusCode.BAD_REQUEST);
            }
            String id = params.get("id").get(0);
            File file = findFile(id);
            if (file != null) {
                InputStream inputStream = new FileInputStream(file);
                if (inputStream != null) {
                    return HttpResp.newRawInstance(inputStream, file.length(), extra);
                }
            }
        } catch (Exception e) {
            Logger.error(e);
        }
        Logger.info(TAG + "[file] : Not FOUND");
        return HttpResp.newInstance(HttpStatusCode.FORBIDDEN);
    }

    private File findFile(String id) {
        File file = new File(ConfigManager.CONFIG_PATH + "/" + ConfigManager.PREVIOUS, id);
        if (file.exists()) {
            return file;
        }
        file = new File(ConfigManager.CONFIG_PATH + "/" + ConfigManager.SUBSEQUENT, id);
        if (file.exists()) {
            return file;
        }
        return null;
    }
}
