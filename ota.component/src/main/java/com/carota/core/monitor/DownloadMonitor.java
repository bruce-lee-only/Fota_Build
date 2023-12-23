/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.core.monitor;

import com.carota.core.ClientState;
import com.carota.core.IDownloadCallback;
import com.carota.core.ISession;
import com.carota.core.ITask;
import com.carota.core.data.CoreStatus;
import com.carota.core.data.UpdateTask;
import com.carota.core.remote.IActionMDA;
import com.carota.core.remote.info.DownloadProgress;
import com.momock.util.Logger;

import java.util.concurrent.atomic.AtomicBoolean;

public class DownloadMonitor {

    private class Observer extends Thread {

        private ISession mSession;

        private Observer(ISession s) {
            mSession = s;
        }

        @Override
        public void run() {
            AtomicBoolean ret = new AtomicBoolean(false);
            try {
                if(!mMaster.downloadPackageStart(mSession.getUSID())) {
                    Logger.error("DownMoniter START @ Fail");
                    boolean b = mMaster.downloadPackageStop();
                    Logger.error("DownMoniter Stop @ %b",b);
                    throw new InterruptedException("DownMoniter START @ Fail");
                }
                Thread.sleep(5000);
                while (doDownloadQuery(mSession, ret)) {
                    mCallback.onProcess(mSession);
                    Thread.sleep(2 * 1000);
                }
                // update last state
                mCallback.onProcess(mSession);
                if(ret.get()) {
                    mStatus.setDownloadState(ClientState.DOWNLOAD_STATE_COMPLETE);
                } else {
                    mStatus.setDownloadState(ClientState.DOWNLOAD_STATE_ERROR);
                }
            } catch (Exception e) {
                ret.set(false);
                mStatus.setDownloadState(ClientState.DOWNLOAD_STATE_IDLE);
            } finally {
                mStatus.setPackageReady(ret.get());
                mCallback.onFinished(mSession, ret.get());
            }
            Logger.debug("DownMoniter END");
        }
    }


    private IActionMDA mMaster;
    private Thread mWorker;
    private IDownloadCallback mCallback;
    private CoreStatus mStatus;

    public DownloadMonitor(IActionMDA mda, CoreStatus status) {
        mMaster = mda;
        mStatus = status;
    }

    public synchronized boolean startDownload(ISession s, IDownloadCallback callback) {
        if(null == mMaster || null == s) {
            Logger.error("DownMoniter START @ Error Params");
            return false;
        }
        int count = s.getTaskCount();
        if(count < 0) {
            Logger.error("DownMoniter START @ Empty Task");
            return false;
        }
        mCallback = callback;
        if(null == mWorker || !mWorker.isAlive()) {
            mStatus.setDownloadState(ClientState.DOWNLOAD_STATE_RUNNING);
            mWorker = new Observer(s);
            mWorker.start();
        }
        return true;
    }

    public boolean pauseDownload(ISession session) {
        Logger.debug("DownMoniter PAUSE");

        if(null != mWorker) {
            mWorker.interrupt();
        }
        if (mMaster.downloadPackageStop()) {
            Logger.info("DownMoniter STOP");
            return true;
        }
        Logger.error("DownMoniter STOP Fail");
        return false;
    }

    private boolean doDownloadQuery(ISession s, AtomicBoolean ret) throws InterruptedException {
        Logger.debug("DownMoniter QUERY");
        ret.set(false);
        int taskCount = s.getTaskCount();
        DownloadProgress dp = mMaster.downloadPackageQuery();
        if(dp == null) {
            throw  new InterruptedException("connect err");
        }

        int state = dp.getState();

        for (int i = 0; i < taskCount; i++) {
            UpdateTask ut = (UpdateTask) s.getTask(i);
            String name = ut.getProp(ITask.PROP_NAME);
            int pg = dp.getProgress(name);
            ut.setDownloadProgress(100 == pg ? ClientState.DOWNLOAD_STATE_COMPLETE : state, pg);
            ut.setDownloadSpeed(dp.getSpeed(name));
        }

        if(ClientState.DOWNLOAD_STATE_COMPLETE == state) {
            ret.set(true);
        }
        return ClientState.DOWNLOAD_STATE_RUNNING == state;
    }
}
