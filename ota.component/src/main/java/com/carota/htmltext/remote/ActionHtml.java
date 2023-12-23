/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.htmltext.remote;

import com.carota.dm.down.FileDownloader;
import com.carota.dm.down.IDownCallback;
import com.carota.dm.down.IFileDownloader;
import com.carota.dm.file.app.AppFileManager;
import com.carota.htmltext.data.HtmlData;
import com.carota.util.HttpHelper;
import com.momock.util.JsonHelper;
import com.momock.util.Logger;

import org.json.JSONObject;

import java.io.File;

public class ActionHtml implements IActionHtml {

    @Override
    public boolean downloadHtml(String fileUrl, String md5, File file, int maxRetry) {
        AppFileManager mManager = new AppFileManager(file.getParentFile(),"HTML");
        if (mManager.verifyMd5(file.getName(), md5, (length, fileLength) -> {})) {
            Logger.debug("DT-HTML-EXEC : same");
            return true;
        } else {
            mManager.deleteFile(file.getName());
        }
        String url = fileUrl.replace("{id}", md5);
        FileDownloader mDownloader = new FileDownloader(url, file.getName(), mManager, maxRetry, 0, new IDownCallback() {
            @Override
            public void progress(int speed, long length, long fileLength) {
            }
        });
        try {
            if (mDownloader.start() == IFileDownloader.CODE_SUCCESS) {
                Logger.debug("DT-HTML-EXEC : downloaded");
                return true;
            } else {
                if (!mDownloader.isRun()) {
                    Logger.error("DT-HTML-EXEC : STOP ");
                } else {
                    Logger.error("DT-HTML-EXEC : ERROR");
                }
            }
        } catch (Exception e) {
            Logger.error("DT-HTML-EXEC : EXCP @ ");
            Logger.error(e);
        }
        return false;
    }

    @Override
    public HtmlData getHtmlData(String url, String scheduleId, String language) {
        final String CALL_TAG = "[RPT-HTML-QUERY] ";
        try {
            Logger.info(CALL_TAG + "URI : %1s", url);

            JSONObject root = new JSONObject();
            root.put("upgradeTaskId", scheduleId);
            root.put("lang", language);

            Logger.debug(CALL_TAG + "BODY : %1s", root.toString());
            HttpHelper.Response resp = HttpHelper.doPost(url, null, root);
            Logger.info(CALL_TAG + "RSP : %1d", resp.getStatusCode());
            Logger.info(CALL_TAG + "DATA : %1s", resp.getBody());
            return new HtmlData(JsonHelper.parseObject(resp.getBody()));
        } catch (Exception e) {
            Logger.error(CALL_TAG + e);
        }
        return null;
    }
}
