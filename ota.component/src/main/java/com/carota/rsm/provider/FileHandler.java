/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.rsm.provider;

import com.carota.rsm.RemovableStorageManager;
import com.carota.svr.HttpResp;
import com.carota.svr.PrivStatusCode;
import com.carota.svr.SimpleHandler;
import com.momock.util.Logger;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class FileHandler extends SimpleHandler {

    @Override
    public HttpResp get(String path, Map<String, List<String>> params, Object extra) {
        try {
            Logger.info("RSM-File : " + params);
            List<String> listParams = params.get("id");
            if(null != listParams && listParams.size() > 0) {
                AtomicLong fileLength = new AtomicLong();
                InputStream fis = RemovableStorageManager.get().findFileById(listParams.get(0), fileLength);
                if(null != fis && fileLength.get() > 0) {
                    return HttpResp.newRawInstance(fis, fileLength.get(), extra);
                }
                return HttpResp.newInstance(PrivStatusCode.REQ_TARGET_UNKNOWN);
            }
        } catch (Exception e) {
            Logger.error(e);
        }
        Logger.error("RSM-file : Not Finished");
        return super.get(path, params, extra);
    }
}
