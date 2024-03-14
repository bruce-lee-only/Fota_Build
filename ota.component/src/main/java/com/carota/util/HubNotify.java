/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.util;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Process;
import android.os.SystemClock;
import android.text.TextUtils;

import com.carota.core.remote.ActionSH;
import com.carota.svr.PrivReqHelper;
import com.momock.util.Logger;

import java.util.List;

public class HubNotify implements Handler.Callback{

    private static final int LOOP_CYCLE = 5 * 1000;
    private Handler mHandler;
    private HandlerThread mThread;
    private final String mHubUrl;
    private final int mPort;
    private long mLastTick;
    private byte[] mReqPayload;

    public HubNotify(String hubName, int port) {
        mPort = port;
        mLastTick = 0;
        mReqPayload = null;
        if(!TextUtils.isEmpty(hubName)) {
            mHubUrl = "http://" + hubName + "/register";
        } else {
            mHubUrl = null;
        }

        mThread = new HandlerThread("HubNotify", Process.THREAD_PRIORITY_BACKGROUND);
        mThread.start();
        mHandler = new Handler(mThread.getLooper(), this);
    }

    // http://ota_proxy/register?p=8090&m=ota_master,ota_dm,ota_test
    public HubNotify update(List<String> moduleList) {
        mReqPayload = ActionSH.createRegisterData(mPort, moduleList);
        return this;
    }

    public void start() {
        if(null != mReqPayload) {
            Logger.info("HUB HB start");
            mHandler.sendEmptyMessage(LOOP_CYCLE);
        }
    }

    public void stop() {
        Logger.info("HUB HB stop");
        mThread.quit();
    }

    @Override
    public boolean handleMessage(Message msg) {
        long curTick = SystemClock.elapsedRealtime();
        if(curTick - mLastTick > LOOP_CYCLE) {
            if (null != mReqPayload) {
                PrivReqHelper.doPost(mHubUrl, mReqPayload);
            }
            mHandler.sendEmptyMessageDelayed(LOOP_CYCLE, LOOP_CYCLE);
        }
        return true;
    }
}
