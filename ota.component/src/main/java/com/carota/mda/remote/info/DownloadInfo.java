/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.mda.remote.info;

import com.carota.protobuf.DownloadManager;

public class DownloadInfo implements IDownloadStatus.IDownloadInfo {

    private String mID;
    private int mState;
    private DownloadManager.ProgressRsp.Work mRaw;


    public DownloadInfo(DownloadManager.ProgressRsp.Work work) {
        mRaw = work;
        mID = work.getId();
        mState = convertState(work);
    }

    @Override
    public int getProgress() {
        return mRaw.getProgress();
    }
	
	@Override
    public int getSpeed() {
        return mRaw.getSpeed();
    }

    @Override
    public int getState() {
        return mState;
    }

    @Override
    public String getId() {
        return mID;
    }

    private int convertState(DownloadManager.ProgressRsp.Work work) {
        switch (work.getStatusValue()) {
            case DownloadManager.ProgressRsp.Work.Status.IDLE_VALUE:
                return IDownloadStatus.DL_STATE_IDLE;
            case DownloadManager.ProgressRsp.Work.Status.WAIT_VALUE:
                return IDownloadStatus.DL_STATE_WAIT;
            case DownloadManager.ProgressRsp.Work.Status.RUN_VALUE:
                return IDownloadStatus.DL_STATE_RUNNING;
            case DownloadManager.ProgressRsp.Work.Status.FINISH_VALUE:
                return IDownloadStatus.DL_STATE_FINISHED;
            default:
                return IDownloadStatus.DL_STATE_ERROR;
        }
    }

    @Override
    public String toString() {
        return mID + " @ " + mState
                + "; PG = " + (mRaw != null ? mRaw.getProgress() : -1)
                + "; SP = " + (mRaw != null ? mRaw.getSpeed() : -1) ;
    }
}
