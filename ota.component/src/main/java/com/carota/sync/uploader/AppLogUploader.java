/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.sync.uploader;

import com.carota.mda.remote.ActionAPI;
import com.carota.mda.remote.IActionAPI;
import com.carota.sync.base.FileDataLogger;
import com.momock.util.JsonDatabase;

import java.io.File;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;

public class AppLogUploader extends FileDataLogger {

    private IActionAPI mActionAPI;
    private String mUrl;

    public AppLogUploader(JsonDatabase.Collection col, File dir, String url) {
        super(col, "AppLog", 512 * 1024, dir);
        mActionAPI = new ActionAPI();
        mUrl = url;
    }

    public boolean LogFile(String rid, File file, int type) {
        return recordFileData(rid, file, "" + type);
    }

    @Override
    protected boolean send(FileMeta meta, String md5, InputStream body) throws ExecutionException {
        return mActionAPI.uploadLogFile(mUrl, meta.getRequestId(),
                meta.getBlockNum(), meta.getBlockIndex(), md5, body, meta.getExtra());
    }
}
