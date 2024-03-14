/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.mda.deploy;

import android.os.SystemClock;
import android.text.TextUtils;

import com.carota.mda.remote.IActionSDA;
import com.carota.mda.remote.info.BomInfo;
import com.carota.mda.remote.info.EcuInfo;
import com.carota.mda.remote.info.SlaveInstallResult;
import com.momock.util.Logger;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

public class DeviceUpdater implements Callable<Integer>{

    private class TryAgain extends RuntimeException {
        public TryAgain(String message) {
            super(message);
        }
    }

    public interface IEventListener {
        void onProcess(int pro);
    }

    public static final int RET_FAILURE = -2;
    public static final int RET_ERROR = -1;
    public static final int RET_TIMEOUT = 0;
    public static final int RET_SUCCESS = 1;

    private static final int INDEX_ID = 0;
    private static final int INDEX_VER = 1;
    private static final int INDEX_SIGN = 2;

    private String mSlaveHost;
    private String mSlaveName;
    private String mDmHost;
    private String[] mTgt;
    private String[] mSrc;
    private IActionSDA mDeviceAdapter;
    private long mTimeout;
    private int mDomain;
    private int mStep;
    private int mProgress;
    private IEventListener mEventListener;
    private Object mData;
    private boolean mResume;
    private int mErrorCode = 0;
    private BomInfo mBomInfo;

    private IInstallListener mInstallListener;

    public DeviceUpdater(IActionSDA adapter, long timeout, BomInfo bomInfo) {
        this(adapter, timeout, false, null, bomInfo);
    }

    public DeviceUpdater(IActionSDA adapter, long timeout, boolean resume, IEventListener listener, BomInfo bomInfo) {
        mDeviceAdapter = adapter;
        mTimeout = timeout > 0 ? timeout : 0;
        mDomain = 0;
        mStep = 0;
        mTgt = new String[3];
        mSrc = new String[3];
        mEventListener = listener;
        mData = null;
        mProgress = 0;
        mResume = resume;
        mBomInfo = bomInfo;
    }

    public DeviceUpdater setResume(boolean resume) {
        mResume = resume;
        return this;
    }

    public DeviceUpdater setDevice(String devHost, String devName, String dmHost, int domain) {
        mSlaveHost = devHost;
        mSlaveName = devName;
        mDmHost = dmHost;
        mDomain = domain;
        return this;
    }

    public DeviceUpdater setTarget(String targetId, String targetVer, String targetSign) {
        mTgt[INDEX_ID] = targetId;
        mTgt[INDEX_VER] = targetVer;
        mTgt[INDEX_SIGN] = targetSign;
        return this;
    }

    public DeviceUpdater setSource(String sourceId, String sourceVer, String sourceSign) {
        mSrc[INDEX_ID] = sourceId;
        mSrc[INDEX_VER] = sourceVer;
        mSrc[INDEX_SIGN]  = sourceSign;
        return this;
    }

    public String getSlaveName() {
        return mSlaveName;
    }

    public int getStep() {
        return mStep;
    }

    public int getProgress() {
        return mProgress;
    }

    public Object getData() {
        return mData;
    }

    public int getErrorCode() {
        return mErrorCode;
    }

    @Override
    public Integer call() {
        if(!verifyParameter()) {
            return RET_ERROR;
        }

        AtomicInteger initRet = new AtomicInteger();
        boolean waitRet = false;

        long timeCurrent = SystemClock.elapsedRealtime();
        long timeTarget = timeCurrent + mTimeout;

        do {
            try {
                Thread.sleep(3 * 1000);

                timeCurrent =  SystemClock.elapsedRealtime();

                SlaveInstallResult sir = mDeviceAdapter.queryInstallResult(mSlaveHost, mSlaveName);

                if (null == sir) {
                    Logger.error("DevUp : Not Available");
                    continue;
                }
                mStep = sir.getStepByName(mSlaveName);
                mErrorCode = sir.getErrorCodeByName(mSlaveName);
                // Query Result
                if (waitRet) {
                    return waitUpgradeResult(sir);
                }

                // Start Upgrade Process
                if (initUpgradeProcess(sir, initRet)) {
                    // Mark As Triggered, Wait Result
                    waitRet = true;
                } else {
                    return initRet.get();
                }
            } catch (TryAgain ta) {
                timeTarget = timeCurrent + mTimeout;
            } catch (InterruptedException ie) {
                // CAN NOT BE INTERRUPTED
            }
        } while (timeTarget > timeCurrent);
        Logger.error("DevUp : ER @ Timeout");
        return RET_TIMEOUT;
    }

    private boolean verifyParameter() {
        if(null == mDeviceAdapter) {
            Logger.error("DevUp : ER @ Adapter");
            return false;
        }
        if(TextUtils.isEmpty(mDmHost)) {
            Logger.error("DevUp : ER @ DM Host");
            return false;
        }
        if(TextUtils.isEmpty(mSlaveName)) {
            Logger.error("DevUp : ER @ Slave Name");
            return false;
        }
        if(TextUtils.isEmpty(mSlaveHost)) {
            Logger.error("DevUp : ER @ Slave Host");
            return false;
        }
        return true;
    }

    private void setProgress(int pg) {
        if (pg != mProgress) {
            mProgress = pg;
            if(null != mEventListener) {
                Logger.debug("DevUp : Pg - " + mProgress);
                mEventListener.onProcess(mProgress);
            }
        }
    }

    public void setEventListener(IEventListener listener, Object data) {
        mEventListener = listener;
        mData = data;
    }
    public void setInstallListener(IInstallListener listener) {
        mInstallListener = listener;
    }

    private boolean initUpgradeProcess(SlaveInstallResult sir, AtomicInteger result) {
        int status = sir.getStatusByName(mSlaveName);
        // trigger upgrade process if Upgrade is not running right now
        if (SlaveInstallResult.STATUS_UPGRADE != status
                && SlaveInstallResult.STATUS_ROLLBACK != status
                && SlaveInstallResult.STATUS_DOWNLOAD != status) {

            EcuInfo ei = mDeviceAdapter.queryInfo(mSlaveHost, mSlaveName, mBomInfo);
            if (null != ei && ei.swVer.equals(mTgt[INDEX_VER])) {
                Logger.debug("DevUp : Updated");
                setProgress(100);
                result.set(RET_SUCCESS);
                return false;
            }

            if (mResume && mStep == SlaveInstallResult.STEP_REBOOT) {
                Logger.debug("DevUp : Resume");
            } else {
                if (mInstallListener !=null && mBomInfo!=null
                        &&!mInstallListener.beforeInstall(mSlaveName,mBomInfo)){
                    Logger.error("DevUp : ER @ PTrigger");
                    result.set(RET_ERROR);
                    return false;
                }
                int trgRet = mDeviceAdapter.triggerInstall(mSlaveHost, mDmHost, mSlaveName, mDomain,
                        mTgt[INDEX_ID], mTgt[INDEX_VER],
                        mSrc[INDEX_ID], mSrc[INDEX_VER],  mTgt[INDEX_SIGN], mSrc[INDEX_SIGN], mBomInfo);

                if (IActionSDA.RET_INS_FAIL == trgRet) {
                    Logger.error("DevUp : ER @ Trigger");
                    result.set(RET_ERROR);
                    return false;
                }
                Logger.debug("DevUp : Start");
            }
            return true;
        }
        Logger.error("DevUp : Retry @ INIT");
        throw new TryAgain("Wait Available");
    }

    private int waitUpgradeResult(SlaveInstallResult sir) {
        int status = sir.getStatusByName(mSlaveName);
        setProgress(sir.getProgress(mSlaveName));
        if(SlaveInstallResult.STATUS_SUCCESS == status) {
            Logger.debug("DevUp : Success");
            setProgress(100);
            mStep = SlaveInstallResult.STEP_NONE;
            return RET_SUCCESS;
        } else if(SlaveInstallResult.STATUS_IDLE == status
                || SlaveInstallResult.STATUS_ERROR == status
                || SlaveInstallResult.STATUS_FAILURE == status) {
            Logger.error("DevUp : ER @ Ret");
            return RET_FAILURE;
        }
        Logger.error("DevUp : Retry @ RET");
        throw new TryAgain("Wait Available");
    }

    public interface IInstallListener {
        boolean beforeInstall(String name, BomInfo bomInfo);
    }
}
