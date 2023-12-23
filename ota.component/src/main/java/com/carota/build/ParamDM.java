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

import com.momock.util.Convert;
import com.momock.util.FileHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ParamDM extends ConfigParser {

    /**
     * <node id="dm" type="local">
     *     <name>ota_dm</name>
     *     <retry>0</retry>
     *     <limit>10</limit>           <!-- 限速,下载64kb，等待10ms -->
     *     <dir>/cache/carota</dir>    <!-- [OPT] -->
     *     <reserve>1024</reserve>     <!-- [OPT] 1024K -->
     * </node>
     *
     * <node id="dm" type="ftp">
     *     <name>ota_dm_ftp</name>
     *     <retry>0</retry>
     *     <limit>10</limit>           <!-- 限速,下载64kb，等待10ms -->
     *     <reserve>1024</reserve>     <!-- [OPT] 1024K -->
     *
     *     <addr>192.168.1.1</addr>
     *     <port>20002</port>
     *     <account>carota</account>   <!-- Ftp -->
     *     <maxspace>1024</maxspace>   <!-- [OPT] 1024K -->
     * </node>
     */
    public static class Info {
        private static final String TYPE_FTP = "ftp";
        private static final String TYPE_LOCAL = "local";
        private String mType;
        private String mHost;
        private int mPort;
        private int mRetry;
        private int mLimitTime;
        private long mReserveSpace;
        private String mAddr;
        private String mUsername;
        private long mMaxSpace;
        private String mDownloadDir;

        public Info(String type) {
            this.mType = type;
            mReserveSpace = -1;
        }

        public boolean isFtp() {
            return TYPE_FTP.equals(mType);
        }

        public String getType() {
            return mType;
        }

        public String getHost() {
            return mHost;
        }

        public int getPort() {
            return mPort == 0 ? 80 : mPort;
        }

        public int getRetry() {
            return mRetry;
        }

        public int getLimitTime() {
            return mLimitTime;
        }

        public long getReserveSpace() {
            return mReserveSpace;
        }

        public String getAddr() {
            return TextUtils.isEmpty(mAddr) ? "127.0.0.1" : mAddr;
        }

        public String getUsername() {
            return mUsername;
        }

        public long getMaxSpace() {
            return mMaxSpace <= 0 ? Long.MAX_VALUE : mMaxSpace;
        }
    }

    public static final String FTP = "ftp";
    private String mDownloadDir;
    private final List<Info> mExtra;
    private Info mInfo;

    public ParamDM() {
        super("dms");
        mExtra = new ArrayList<>();
    }

    @Override
    protected void setType(String type) {
        if (Info.TYPE_FTP.equals(type) || Info.TYPE_LOCAL.equals(type)) {
            mInfo = new Info(type);
            mExtra.add(mInfo);
        }
    }

    @Override
    protected void set(String tag, String name, String val, boolean enabled) {
        if (mInfo == null) {
            mInfo = new Info(Info.TYPE_LOCAL);
            mExtra.add(mInfo);
        }
        switch (tag) {
            case "name":
                mInfo.mHost = val;
                break;
            case "addr":
                mInfo.mAddr = val;
                break;
            case "port":
                mInfo.mPort = Convert.toInteger(val, 80);
                break;
            case "retry":
                mInfo.mRetry = Convert.toInteger(val, 0);
                break;
            case "dir":
                mDownloadDir = val;
                break;
            case "reserve":
                mInfo.mReserveSpace = Convert.toLong(val) << 10;
                break;
            case "limit":
                mInfo.mLimitTime = Convert.toInteger(val);
                break;
            case "account":
                mInfo.mUsername = val;
                break;
            case "maxspace":
                mInfo.mMaxSpace = Convert.toLong(val) << 10;
                break;
        }
    }

    public File getDownloadDir(Context context) {
        File ret;
        if(null == mDownloadDir) {
            ret = new File(context.getFilesDir(), "dm");
        } else {
            ret = new File(mDownloadDir);
        }
        FileHelper.mkdir(ret);
        return ret;
    }

    public List<Info> getDmList() {
        return mExtra;
    }

    public Info findDmInfo(String host) {
        if (host != null) {
            for (Info info : mExtra) {
                if (host.equals(info.mHost)) return info;
            }
        }
        return null;
    }
}
