/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.build;

import android.content.Context;
import android.text.TextUtils;

import com.carota.dm.down.FileDownloader;
import com.momock.util.Convert;
import com.momock.util.FileHelper;

import java.io.File;

public class ParamDM extends ConfigParser {

    /**
     * <node id="dm" >
     * <name>ota_dm</name>
     * <retry>0</retry>
     * <limit>2048</limit>         <!-- KB/S -->
     * <dir>/cache/carota</dir>    <!-- [OPT] -->
     * <reserve>1024</reserve>     <!-- [OPT] 1024K -->
     * </node>
     */
    private String mDownloadDir;
    private String mHost;
    private int mRetry;
    private Long mLimitSpeed;
    private long mReserveSpace;

    public ParamDM() {
        super("dm");
        mReserveSpace = -1;
        mLimitSpeed = Long.MAX_VALUE;
    }

    public String getHost() {
        return mHost;
    }

    public int getRetry() {
        return mRetry;
    }

    public long getLimitSpeed() {
        return mLimitSpeed;
    }

    public long getReserveSpace() {
        return mReserveSpace;
    }

    @Override
    protected void set(String tag, String name, String val, boolean enabled) {
        switch (tag) {
            case "name":
                mHost = val;
                break;
            case "retry":
                mRetry = Convert.toInteger(val, 0);
                break;
            case "dir":
                if (!TextUtils.isEmpty(val)) mDownloadDir = val;
                break;
            case "reserve":
                mReserveSpace = Convert.toLong(val) << 10;
                break;
            case "limit":
                mLimitSpeed = Math.max(FileDownloader.DEFAULT_BUFFER_SIZE,Convert.toLong(val) << 10);
                break;
        }
    }

    public File getDownloadDir(Context context) {
        File ret;
        if (null == mDownloadDir) {
            ret = new File(context.getFilesDir(), "dm");
        } else {
            ret = new File(mDownloadDir);
        }
        FileHelper.mkdir(ret);
        return ret;
    }

}
