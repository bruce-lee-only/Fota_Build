/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.dm.task;

import android.text.TextUtils;

public class TaskInfo {

    public final static int STATUS_ERROR = -1;
    public final static int STATUS_IDLE = 0;
    public final static int STATUS_WAIT = 1;
    public final static int STATUS_RUNNING = 2;
    public final static int STATUS_FINISH = 3;

    private final String mId;
    private final String mUrl;

    private String mVerify;
    private String mDesc;
    private int mSpeed;

    private int mProgress;
    private int mTaskStatus;
    private boolean mDestory;

    private TaskInfo(String id, String url) {
        mId = null == id ? "" : id;
        mUrl = null == url ? "" : url;
        mVerify = "";
        mDesc = "";
        mProgress = 0;
        mTaskStatus = STATUS_IDLE;
    }

    public int getSpeed() {
        return mSpeed;
    }

    public void setmSpeed(int mSpeed) {
        this.mSpeed = mSpeed;
    }

    void setStatus(int status) {
        mTaskStatus = status;
        if (mTaskStatus == STATUS_FINISH) {
            mProgress = 100;
            mSpeed = 0;
        } else if (mTaskStatus == STATUS_ERROR) {
            mSpeed = 0;
        } else if (mTaskStatus == STATUS_WAIT){
            mProgress = 0;
            mSpeed = 0;
        }
    }

    void setProgress(int progress) {
        if (progress < 0) {

        } else if (progress < 100) {
            mProgress = progress;
        } else {
            mProgress = 99;
        }
    }

    public String getTmpFileName() {
        return getFileName().concat(".tmp");
    }

    public String getFileName() {
        return TextUtils.isEmpty(mVerify) ? mId : mVerify;
    }


//    public void refresh() {
//        if (!mFileManager.existsFile(mId, null) && !mFileManager.existsFile(getIdTmp(), null)) {
//            Logger.error("DT-MGR-TASK : RESET @ " + mId);
//            mProgress = 0;
//            mSpeed = 0;
//            if (STATUS_FINISH == mTaskStatus) {
//                mTaskStatus = STATUS_IDLE;
//            }
//        }
//    }

    public String getId() {
        return mId;
    }

    public String getUrl() {
        return mUrl;
    }

    public String getVerify() {
        return mVerify;
    }

    public String getDesc() {
        return mDesc;
    }

    public int getStatus() {
        return mTaskStatus;
    }

    public int getProgress() {
        return mProgress;
    }

    @Override
    public String toString() {
        return "Info{" +
                "id='" + mId + '\'' +
                ", url='" + mUrl + '\'' +
                ", md5='" + mVerify + '\'' +
                ", desc='" + mDesc + '\'' +
                ", status=" + mTaskStatus +
                '}';
    }

    public static class Builder {

        private TaskInfo mInfo;

        public Builder(String id, String url) {
            mInfo = new TaskInfo(id, url);
        }

        public Builder setVerify(String md5) {
            if(null != md5) {
                mInfo.mVerify = md5;
            }
            return this;
        }

        public Builder setDesc(String desc) {
            if(null != desc) {
                mInfo.mDesc = desc;
            }
            return this;
        }

        public Builder setStatus(int status) {
            mInfo.mTaskStatus = status;
            return this;
        }

        TaskInfo build() {
            if (mInfo.mId.isEmpty() || mInfo.mUrl.isEmpty()) {
                return null;
            }
            return mInfo;
        }
    }

}
