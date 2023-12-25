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

import android.content.Context;
import android.os.SystemClock;

import com.carota.core.ClientState;
import com.carota.core.ISession;
import com.carota.core.ITask;
import com.carota.core.data.UpdateTask;
import com.carota.core.remote.IActionMDA;
import com.carota.core.remote.info.InstallProgress;
import com.carota.html.HtmlHelper;
import com.momock.util.Logger;

import java.util.Map;


public class InstallMonitor {

    public interface IEvent {
        void onStart(ISession s);
        void onTrigger(ISession s);
        void onProcess(ISession s, int status, int successCount);
        void onStop(ISession s, boolean cancel, int status);
    }

    private class Observer extends Thread {

        private ISession mSession;
        private boolean mResume;
        private final Context mContext;

        private Observer(ISession s, boolean resume, Context context) {
            mSession = s;
            mResume = resume;
            mContext = context;
        }

        @Override
        public void run() {
            int state = ClientState.UPGRADE_STATE_IDLE;
            boolean stopped = false;
            try {
                state = doUpgrade(mResume, mSession);
                if (state == ClientState.UPGRADE_STATE_SUCCESS) {
                    HtmlHelper.setSuccessHtml(mContext);
                }
            } catch (InterruptedException e) {
                stopped = true;
                state = ClientState.UPGRADE_STATE_IDLE;
            } catch (Exception e) {
                // unknown error
                state = ClientState.UPGRADE_STATE_ERROR;
            } finally {
                mListener.onStop(mSession, stopped, state);
            }
            Logger.debug("InsMoniter END");
        }
    }

    private IActionMDA mMaster;
    private long mRebootTimeout;
    private Thread mWorker;
    private IEvent mListener;

    public InstallMonitor(IActionMDA master, long rebootTimeout) {
        mMaster = master;
        mRebootTimeout = rebootTimeout;
        mWorker = null;
    }

    public boolean isRunning() {
        return null != mWorker && mWorker.isAlive();
    }

    public boolean triggerUpgrade(boolean resume, ISession session, IEvent listener, Context context) {
        mListener = listener;
        synchronized (this) {
            if (null == mWorker || !mWorker.isAlive()) {
                Logger.debug("InsMoniter SET worker");
                mListener.onStart(session);
                mWorker = new Observer(session, resume,context);
                mWorker.start();
                return true;
            }
            Logger.debug("InsMoniter WORKING");
        }
        return false;
    }

    public void stopMonitorUpgrade() {
        synchronized (this) {
            Logger.debug("InsMoniter STOP");
            if (null != mWorker) {
                mWorker.interrupt();
                mWorker = null;
            }
        }
    }

    private int doUpgrade(boolean resume, ISession s) throws InterruptedException{
        String usid = s.getUSID();
        Logger.debug("InsMoniter DO start @ " + resume);
        if(!resume) {
            Logger.debug("InsMoniter DO master");
            mListener.onTrigger(s);
            if (!mMaster.upgradeEcuInMaster(usid)) {
                return ClientState.UPGRADE_STATE_ERROR;
            }
        }

        Logger.debug("InsMoniter DO query");
        long curTime = 0;
        long targetTime = SystemClock.elapsedRealtime() + mRebootTimeout;

        while (curTime < targetTime) {
            curTime = SystemClock.elapsedRealtime();
            Thread.sleep(10 * 1000);
            InstallProgress progress = mMaster.queryUpdateStatus();
            if(null != progress) {
                int successCount = 0;
                Map<String, InstallProgress.Detail> pgDetail = progress.getDetail();
                // refresh status in ITask
                for (int i = 0; i < s.getTaskCount(); i++) {
                    UpdateTask task = (UpdateTask) s.getTask(i);
                    InstallProgress.Detail d = pgDetail.get((String)task.getProp(ITask.PROP_NAME));
                    if(null != d) {
                        task.setInstallState(d.state, d.progress);
                        if (ClientState.UPGRADE_STATE_SUCCESS == d.state) {
                            successCount++;
                        }
                    }
                }

                int state = progress.getTotalState();

                // if upgrade process is finished
                if(ClientState.UPGRADE_STATE_UPGRADE != state
                        && ClientState.UPGRADE_STATE_ROLLBACK != state) {
                    mListener.onProcess(s, state, successCount);
                    Thread.sleep(3 * 1000);
                    Logger.debug("InsMoniter DO stop @ " + state);
                    return state;
                } else {
                    mListener.onProcess(s, state, successCount);
                }
                targetTime = curTime + mRebootTimeout;
            }
        }
        Logger.debug("InsMoniter DO error");
        return ClientState.UPGRADE_STATE_ERROR;
    }
}
