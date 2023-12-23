/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.sda;

import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Base64;

import com.carota.agent.IRemoteAgent;
import com.carota.agent.RemoteAgent;
import com.carota.mda.remote.ActionDM;
import com.carota.mda.security.ISecuritySolution;
import com.carota.sda.util.RemoteAgentCallback;
import com.carota.sda.util.RemoteAgentServiceHolder;
import com.carota.sda.util.RemoteAgentServiceManager;
import com.carota.sda.util.SlaveEvent;
import com.momock.util.EncryptHelper;
import com.momock.util.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public final class UpdateSlave implements ISlaveDownloadAgent, Runnable {

    private static class Supervisor extends Thread {
        private SlaveTask task;
        private boolean triggered;

        public Supervisor(SlaveTask task, Runnable r) {
            super(r);
            this.task = task;
            triggered = false;
        }
    }

    private static final int DEFAULT_DOMAIN = 0;

    public static final String INSTALL_MSG_TRANSPORT = "transport";
    public static final String INSTALL_MSG_VERIFY = "verify";
    public static final String INSTALL_MSG_DEPLOY = "deploy";
    public static final String INSTALL_MSG_INTERRUPT = "interrupt";
    public static final String INSTALL_MSG_REBOOT = "reboot";

    private final String mId;
    private final String mHost;
    private final String mAgent;
    private final String mPkg;
    private final long mRebootTimeout;
    private final ISecuritySolution mSolution;
    private final int mMaxInsRetry;
    private Context mContext;
    private File mDownloadDir;
    private SlaveState mState;
    private long mActiveTime;
    private SlaveEvent mSlaveEvent;
    private RemoteAgentServiceHolder mRemoteAgentHolder;
    private Supervisor mWorker;
    private ISlaveMethod mMethod;

    public UpdateSlave(String slaveId, String slaveHost, String slaveAgent,
                       String pkg, long rebootTimeout, int retry,
                       ISlaveMethod method, ISecuritySolution secure) {
        mId = slaveId;
        mHost = slaveHost;
        mAgent = slaveAgent;
        mPkg = pkg;
        mRebootTimeout = rebootTimeout;
        mActiveTime = 0;
        mSlaveEvent = null;
        mRemoteAgentHolder = null;
        mWorker = null;
        mMethod = method;
        mSolution = secure;
        mMaxInsRetry = retry > 0 ? retry : 1;
    }

    @Override
    public void init(Context context, File workDir) {
        mContext = context;
        mDownloadDir = workDir;

        mState = new SlaveState(mId, DEFAULT_DOMAIN);
        resetActiveTime();

        mSlaveEvent = SlaveEvent.getCache(context, mId);

        RemoteAgentServiceManager.get().add(context, mPkg, "ota.intent.action.BIND_RAS");
        mRemoteAgentHolder = RemoteAgentServiceManager.get().findAgentHolder(mPkg);
    }

    private IRemoteAgent getAgent() {
        return mRemoteAgentHolder.getAgent(mContext, mAgent);
    }

    @Override
    public String getId() {
        return mId;
    }

    @Override
    public String getHost() {
        return mHost;
    }

    @Override
    public SlaveInfo readInfo(String ecuName, Bundle bomInfo) {
        return SlaveInfo.fromBundle(mMethod.readInfo(mContext, getAgent(), ecuName, bomInfo));
    }

    @Override
    public synchronized boolean triggerUpgrade(SlaveTask task) {
        if (null != mWorker && mWorker.isAlive()) {
            // upgrade is already running
            return true;
        }
        // clean last before start
        mMethod.finishUpgrade(null, getAgent());
        // init parameter for upgrade
        resetActiveTime();
        // set state for Install Progress
        mState = new SlaveState(task.name, task.domain);
        mState.setState(SlaveState.STATE_UPGRADE, 0);

        mWorker = new Supervisor(task, this);
        mWorker.start();
        return true;
    }

    @Override
    public synchronized SlaveState queryState(String ecuName) {
        if (null == mWorker) {
            refreshAgentState(ecuName);
        } else if (!mWorker.triggered) {
            refreshAgentProgress(ecuName);
        }
        return mState;
    }

    @Override
    public boolean isRunning() {
        return null != mWorker && mWorker.isAlive();
    }

    @Override
    public boolean fetchEvent(String type, int max, List<String> events, List<String> ids) {
        return mSlaveEvent.fetchEvent(type, max, events, ids);
    }

    @Override
    public boolean deleteEvent(List<String> ids) {
        return mSlaveEvent.deleteEvent(ids);
    }

    @Override
    public boolean listLogFiles(String type, int max, List<String> fileNames, String extraPath) {
        IRemoteAgent agent = getAgent();
        try {
            Bundle bundle = new Bundle();
            if (extraPath != null) {
                bundle.putString("extra_path", extraPath);
            }
            if (null != agent && agent.archiveLogs(type, bundle)) {
                File dir = RemoteAgentCallback.getLogCacheDir(mContext, mId);
                for (File f : dir.listFiles()) {
                    fileNames.add(f.getName());
                }
                return true;
            }
        } catch (Exception e) {
            Logger.error(e);
        }
        return false;
    }

    @Override
    public File findLogFile(String name) {
        File dir = RemoteAgentCallback.getLogCacheDir(mContext, mId);
        return new File(dir, name);
    }

    @Override
    public void run() {
        Thread self = Thread.currentThread();
        if (!(self instanceof Supervisor)) {
            Logger.error("SlaveDownloadAgent Invalid Job @ " + mHost);
            return;
        }
        Supervisor worker = ((Supervisor) self);

        try {
            Logger.debug("SlaveDownloadAgent START @ " + mHost);
//            SlaveInfo si = readInfo();
//            String swVer = si.getProp(SlaveInfo.PROP_VER_SW);
//            if(swVer.equals(worker.task.targetVer)) {
//                Logger.debug("SlaveDownloadAgent Install FINISHED @ " + mHost);
//                mState.setState(SlaveState.STATE_SUCCESS);
//            } else {
            mState.setState(SlaveState.STATE_UPGRADE, 0);
            // search target file in DM and RSM
            File target = new File(mDownloadDir, worker.task.targetId);
            if (!target.exists()) {
                InputStream inputStream = ActionDM.openInputStream(worker.task.hostDM, worker.task.targetId);
                if (inputStream == null) {
                    Logger.error("SlaveAgent Missing Target File @ %s", mHost);
                    throw new FileNotFoundException("Missing Target File");
                }
                if (!worker.task.targetId.equals(EncryptHelper.calcFileMd5(inputStream))) {
                    Logger.error("SlaveAgent Missing Target File @ %s", mHost);
                    inputStream.close();
                    throw new FileNotFoundException("Missing Target File");
                }
                inputStream.close();
            }
            Logger.debug("SlaveDownloadAgent Install @ " + mHost);
            Bundle extraData = SlaveTask.toBundle(worker.task);
            extraData.putLong("timeout", mRebootTimeout);
            if (worker.task.applyInfo != null && worker.task.applyInfo.length > 0) {
                extraData.putBoolean("is_sub", true);
            }

            AtomicBoolean verifyResult = new AtomicBoolean(false);
            Logger.debug("SlaveDownloadAgent Verify @ " + mHost);
            if (null != mSolution) {
                File signFile = new File(mDownloadDir, worker.task.targetSign);
                if (target.exists()) {
                    target = mSolution.decryptPackage(verifyResult, target, mDownloadDir, signFile);
                    Logger.debug("SlaveDownloadAgent Decrypt @ " + (null != target) + " - " + verifyResult.get());
                    if (null == target) {
                        throw new RuntimeException("Target Fail to Verify @ " + mHost);
                    }
                } else {
                    if (!signFile.exists()) {
                        signFile = new File(mDownloadDir, "/sign/" + worker.task.targetSign);
                    }
                    Boolean decryptPackage = mSolution.decryptPackage(verifyResult, worker.task.hostDM, worker.task.targetId, signFile);
                    if (!decryptPackage) {
                        throw new RuntimeException("Target Stream Fail to Verify @ " + mHost);
                    }
                }
            } else {
                // verify is disabled by RAS configure
                verifyResult.set(true);
            }
            if (!verifyResult.get()) {
                throw new RuntimeException("Fail to Verify");
            }

            int retryCount = mMaxInsRetry;
            while (retryCount > 0) {
                retryCount--;
                resetActiveTime();
                if (mMethod.startUpgrade(mContext, target, getAgent(), extraData)) {
                    Logger.debug("SlaveDownloadAgent Func Success @ " + mHost);
                    worker.triggered = true;
                    // add gap before query result
                    Thread.sleep(5000);
                    if (worker.task.domain >= 0) {
                        Logger.debug("SlaveAgent Wait Result");
                        if (getResultAndProgress(worker.task.name) || retryCount <= 0) {
                            // success or no need to retry
                            Logger.debug("SlaveAgent Wait upgrade finish");
                            waitUpgradeFinished(worker.task.name);
                        } else {
                            Logger.error("SlaveDownloadAgent Func FAIL & RETRY @ " + mHost);
                            Thread.sleep(5000);
                            continue;
                        }
                    } else {
                        Logger.debug("SlaveAgent Wait Reboot");
                        synchronized (mAgent) {
                            do {
                                mAgent.wait();
                                Logger.error("SlaveAgent Wait Loop");
                                Thread.sleep(10000);
                            } while (true);
                        }
                    }
                    Logger.debug("SlaveDownloadAgent Func FINISHED @ " + mHost);
                    break;
                } else if (retryCount > 0) {
                    Logger.error("SlaveDownloadAgent Func TRIG & RETRY @ " + mHost);
                    Thread.sleep(5000);
                    continue;
                }
                Logger.error("SlaveDownloadAgent Func FAIL @ " + mHost);
                mState.setState(SlaveState.STATE_ERROR, 99);
                break;
            }
            //           }
        } catch (FileNotFoundException e) {
            Logger.error(e);
            mState.setState(SlaveState.STATE_ERROR, 98);
        } catch (Exception e) {
            Logger.error(e);
            mState.setState(SlaveState.STATE_ERROR, 97);
        } finally {
            mMethod.finishUpgrade(null, getAgent());
        }
    }

    private Bundle refreshAgentProgress(String ecuName) {
        synchronized (mId) {
            Bundle data = mMethod.queryStatus(mContext, getAgent(), ecuName);
            mState.setProgress(data.getInt(RemoteAgent.KEY_STATUS_PROGRESS));
            return data;
        }
    }

    private boolean refreshAgentState(String ecuName) {
        Bundle data = refreshAgentProgress(ecuName);
        int ret = data.getInt(RemoteAgent.KEY_STATUS_RESULT, Integer.MIN_VALUE);
        int ec = data.getInt(RemoteAgent.KEY_ERROR_CODE, 0);
        String msg = "";
        String state = null;
        switch (ret) {
            case RemoteAgent.INSTALL_ERROR_UNKNOWN:
                state = SlaveState.STATE_ERROR;
                msg = INSTALL_MSG_INTERRUPT;
                break;
            case RemoteAgent.INSTALL_ERROR_UPGRADE:
                state = SlaveState.STATE_FAILURE;
                msg = INSTALL_MSG_DEPLOY;
                break;
            case RemoteAgent.INSTALL_ERROR_UPLOAD:
            case RemoteAgent.INSTALL_ERROR_DOWNLOAD:
                state = SlaveState.STATE_DOWNLOAD;
                msg = INSTALL_MSG_TRANSPORT;
                break;
            case RemoteAgent.INSTALL_ERROR_VERIFY:
                state = SlaveState.STATE_ERROR;
                msg = INSTALL_MSG_VERIFY;
                break;
            case RemoteAgent.INSTALL_SUCCESS:
                state = SlaveState.STATE_SUCCESS;
                msg = INSTALL_MSG_REBOOT;
                break;
        }

        if(data.getBoolean(RemoteAgent.KEY_STATUS_TRIGGERED)) {
            mState.setMsg(INSTALL_MSG_REBOOT);
        } else {
            mState.setMsg(msg);
        }

        if(null != state) {
            mState.setState(state, ec);
        } else if(getUsedTime() > mRebootTimeout) {
            // Set error when resume timeout.
            mState.setState(SlaveState.STATE_FAILURE, 100);
        } else {
            mState.setState(SlaveState.STATE_UPGRADE, ec);
            return false;
        }
        return true;
    }

    private void waitUpgradeFinished(String ecuName) throws InterruptedException {
        do {
            Thread.sleep(5000);
            if (refreshAgentState(ecuName)) {
                break;
            }
        } while (true);
    }

    private boolean getResultAndProgress(String ecuName) throws InterruptedException {
        do {
            Thread.sleep(5000);
            Bundle data = refreshAgentProgress(ecuName);
            int ret = data.getInt(RemoteAgent.KEY_STATUS_RESULT, Integer.MIN_VALUE);
            Logger.debug("********** slave update ret: " + ret);
            if (RemoteAgent.INSTALL_WAIT == ret || Integer.MIN_VALUE == ret) {
                Logger.debug("********** slave update time: " + getUsedTime());
                Logger.debug("********** mRebootTimeout: " + mRebootTimeout);
                if(getUsedTime() < mRebootTimeout) {
                    continue;
                }
                ret = RemoteAgent.INSTALL_ERROR_UNKNOWN;
            }
            return RemoteAgent.INSTALL_SUCCESS == ret;
        } while (true);
    }

    private void resetActiveTime() {
        mActiveTime = SystemClock.elapsedRealtime();
    }

    private long getUsedTime() {
        return SystemClock.elapsedRealtime() - mActiveTime;
    }
}
