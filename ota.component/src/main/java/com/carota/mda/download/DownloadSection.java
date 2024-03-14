/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.mda.download;

import com.carota.mda.remote.IActionDM;
import com.carota.mda.remote.info.IDownloadStatus;

import java.util.ArrayList;
import java.util.List;

public class DownloadSection implements IDownloadSection {

    private final int mIndex;
    private final String mName;
    private int mProgress;
    private List<String[]> mFileInfo;    // String[] {id, url, checksum, description}
    private IActionDM mManager;
    private int mErrorCode;
	private int mSpeed;

    public DownloadSection(String taskName, int taskIndex, IActionDM dm) {
        mName = taskName;
        mIndex = taskIndex;
        mManager = dm;
        mFileInfo = new ArrayList<>();
        mProgress = 0;
        mErrorCode = -1;
		mSpeed = 0;
    }

    public List<String> listFileId() {
        List<String> ret = new ArrayList<>();
        for(String[] fi : mFileInfo) {
            ret.add(fi[0]);
        }
        return ret;
    }

    public void addFile(String id, String url, String checksum, String desc) {
        mFileInfo.add(new String[]{id, url, checksum, desc});
    }

    public boolean download() {
        if(mFileInfo.size() <= 0) {
            return false;
        }
        mProgress = 1;
        for(String[] fi : mFileInfo) {
            if(!mManager.download(fi[0], fi[1], fi[2], fi[3])) {
                mManager.stop(null);
                return false;
            }
        }
        return true;
    }

    /**
     * @return  true - downloading
     *          false - stopped.
     *              if 100 > progress > 0, section is running.
     *              otherwise, stopped.
     */
    public boolean update() {
        IDownloadStatus ds = mManager.queryStatus();
        int pg = 0;
        int finished = 0;
        int speed = 0;
        for(String[] fi : mFileInfo) {
            IDownloadStatus.IDownloadInfo dInfo = ds.getDownloadInfo(fi[0]);
            if(null != dInfo) {
                pg += dInfo.getProgress();
                int state = dInfo.getState();
                if(IDownloadStatus.DL_STATE_FINISHED == state) {
                    mErrorCode = 0;
                    finished++;
                    speed += 0;
                } else if(IDownloadStatus.DL_STATE_ERROR == state) {
                    mErrorCode = 1;
					mProgress = -1;
                    mManager.stop(null);
                    mSpeed = 0;
                    return false;
                } else if(IDownloadStatus.DL_STATE_IDLE == state) {
                    mErrorCode = 2;
                    mProgress = -1;
                    mManager.stop(null);
					mSpeed = 0;
                    return false;
                } else {
                    speed += dInfo.getSpeed();
                }
            }
        }
        mProgress = Math.max(Math.min(pg / mFileInfo.size(), 99), 1);
        mSpeed = speed;
        return mFileInfo.size() != finished;
    }


    @Override
    public String getName() {
        return mName;
    }

    @Override
    public String getDM() {
        return mManager.toString();
    }

    /**
     * @return  [0 - 100] - downloading
     */
    @Override
    public int getProgress() {
        return mProgress;
    }

    @Override
    public int getIndex() {
        return mIndex;
    }

    @Override
    public int getSpeed() {
        return mSpeed;
    }
    public int getFileCount() {
        return mFileInfo.size();
    }

    @Override
    public int getErrorCode () {
        return mErrorCode;
    }
}
