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

import android.text.TextUtils;

import com.carota.dtc.remote.ActionDTC;
import com.carota.dtc.remote.IActionDTC;
import com.carota.dtc.remote.UploadLogCode;
import com.carota.sync.base.FileDataLogger;
import com.momock.util.EncryptHelper;
import com.momock.util.JsonDatabase;
import com.momock.util.Logger;

import java.io.File;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutionException;

public class SysLogUploader extends FileDataLogger {

    private IActionDTC mActionDTC;
    private String mUrl;

    public SysLogUploader(JsonDatabase.Collection col, File dir, String url) {
        super(col, "SysLog", 512 * 1024, dir);
        mActionDTC = new ActionDTC();
        mUrl = url;
    }

    public boolean LogFile(String rid, File file, String vin) {
        Logger.debug("SysLog rid = %s, file = %S, vin = %s", rid, file.getPath(), vin);
        return !TextUtils.isEmpty(vin) && recordFileData(rid, file, vin);
    }

    protected boolean send(FileMeta meta, String md5, InputStream body) throws ExecutionException {
        String vin = meta.getExtra();
        try {
            String secureCode = EncryptHelper.calcFileMd5(((meta.getBlockIndex() + 1)
                    + "-" + meta.getBlockNum() + md5 + meta.getRequestId() + vin).getBytes());
            Logger.debug("upload md5 = " + md5 + " / secureCode = " + secureCode) ;
            int code = mActionDTC.uploadFilterLogFile(mUrl, meta.getRequestId(), meta.getBlockNum(),
                    meta.getBlockIndex() + 1, md5, meta.getChecksum(), secureCode, body);
            if (code == UploadLogCode.UPLOAD_OK.getCode()) {
                Logger.debug("upload " + md5 + UploadLogCode.UPLOAD_OK.getMsg());
                return true;
            } else if (code == UploadLogCode.DATA_EXISTS.getCode()) {
                Logger.error("upload " + md5 + UploadLogCode.DATA_EXISTS.getMsg());
                return true;
            } else if (code == UploadLogCode.SCHEDULE_DISABLED.getCode()) {
                Logger.error("upload " + md5 + UploadLogCode.SCHEDULE_DISABLED.getMsg());
                throw new ExecutionException(new Throwable(UploadLogCode.SCHEDULE_DISABLED.getMsg()));
            } else if (code == UploadLogCode.PRE_CLIENT_LOG_SKIPPED.getCode()) {
                Logger.error("upload " + md5 + UploadLogCode.PRE_CLIENT_LOG_SKIPPED.getMsg());
                //throw new ExecutionException(new Throwable(UploadLogCode.PRE_CLIENT_LOG_SKIPPED.getMsg()));
                //throw new IndexOutOfBoundsException(UploadLogCode.PRE_CLIENT_LOG_SKIPPED.getMsg());
                //throw new RuntimeException(UploadLogCode.PRE_CLIENT_LOG_SKIPPED.getMsg());
                return false;
            } else {
                Logger.error("upload unknown error code = %d", code);
                if (code != -1) {
                    throw new ExecutionException(new Throwable("unknown error code = " + code));
                } else {
                    return false;
                }
            }
        } catch (NoSuchAlgorithmException e) {
            return false;
        }
    }

}
