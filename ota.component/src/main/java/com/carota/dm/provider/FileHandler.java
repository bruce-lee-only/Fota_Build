/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.dm.provider;

import android.content.Context;

import com.carota.dm.task.ITaskManager;
import com.carota.svr.HttpResp;
import com.momock.util.Logger;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import fi.iki.elonen.HttpStatusCode;

public class FileHandler extends BaseHandler {

    public FileHandler(Context context, ITaskManager tm) {
        super(context, tm);
    }

    @Override
    public HttpResp get(String path, Map<String, List<String>> params, Object extra) {
        Logger.info("DM-Console[file] : FileHandler");
        try {
            Logger.info("DM-Console[dl] : FileHandler - " + params);
            if (params.isEmpty() || !params.containsKey("id")) {
                return HttpResp.newInstance(HttpStatusCode.BAD_REQUEST);
            }
            String id = params.get("id").get(0);
            long length = mTm.findFileInputLengthById(id);
            InputStream inputStream = mTm.findFileInputStreamById(id);
            if (inputStream != null) {
                return HttpResp.newRawInstance(inputStream, length, extra);
            }
        } catch (Exception e) {
            Logger.error(e);
        }
        Logger.info("DM-Console[file] : Not Finished");
        return HttpResp.newInstance(HttpStatusCode.FORBIDDEN);
    }
}
