/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.util.svr;

import android.content.Context;

import com.carota.dev.R;
import com.carota.svr.HttpResp;
import com.carota.svr.PrivStatusCode;
import com.carota.svr.SimpleHandler;
import com.momock.util.EncryptHelper;
import com.momock.util.FileHelper;
import com.momock.util.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class DebugHandler extends SimpleHandler {
    private final Context mContext;

    public DebugHandler(Context context) {
        super();
        mContext = context;
    }

    @Override
    public HttpResp get(String path, Map<String, List<String>> params, Object extra) {
        List<String> data = params.get("m");
        Logger.info("Handler GET Params : %s", data);
        if(null == data || data.get(0).equalsIgnoreCase("GET")) {
            //return HttpResp.newInstance(200, mContext.getString(R.string.http_resp_body_ok));
            return HttpResp.newInstance(PrivStatusCode.OK);
        } else if(data.get(0).equalsIgnoreCase("DL")) {
            try {
                InputStream is = mContext.getResources().openRawResource(R.raw.file);
                Logger.info("Download File Length = %d", is.available());
                //return HttpResp.newInstance(is, is.available(), extra);
                File srcFile = new File(mContext.getFilesDir(), "src.demo");
                if(!srcFile.exists()){
                    FileHelper.copy(is, srcFile);
                    Logger.error("SRC FILE MD5 = %s", EncryptHelper.calcFileMd5(srcFile));
                }
                return HttpResp.newRawInstance(new FileInputStream(srcFile), srcFile.length(), extra);
            } catch (Exception e) {
                Logger.error(e);
            }
        } else if(data.get(0).equalsIgnoreCase("DELAY")) {
            try {
                Thread.sleep(10 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return HttpResp.newInstance(PrivStatusCode.OK);
        }
        return HttpResp.newInstance(PrivStatusCode.REQ_TARGET_UNKNOWN);
    }

    @Override
    public HttpResp post(String path, Map<String, List<String>> params, byte[] body, Object extra) {
        Logger.debug("Handler POST : %s", new String(body));
        return HttpResp.newInstance(PrivStatusCode.OK, mContext.getString(R.string.http_resp_body_ok));
        //return RESP_OK;
    }

}
