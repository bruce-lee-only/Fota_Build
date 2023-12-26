/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.sync.util;

import android.text.TextUtils;

import com.carota.sync.base.FileDataLogger;
import com.momock.util.JsonHelper;
import com.momock.util.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public abstract class MaterialCollector<T extends FileDataLogger> {

    public static class CampaignStatus {

        public static final String KEY_TOKEN = "token";
        private static final String KEY_STEP = "step";
        private static final String KEY_EXTRA= "exra";

        private String mToken;
        private int mStep;
        private String mExtra;

        private CampaignStatus(JSONObject raw) {
            if(null == raw) {
                initToken("");
            } else {
                mToken = raw.optString(KEY_TOKEN);
                mStep = raw.optInt(KEY_STEP);
                mExtra = raw.optString(KEY_EXTRA);
            }
        }

        void initToken(String token) {
            mToken = token;
            mStep = 0;
            mExtra = "";
        }

        void setStep(int step) {
            mStep = step;
        }

        public void setExtra(String extra) {
            mExtra = extra;
        }

        public String getToken() {
            return mToken;
        }

        int getStep() {
            return mStep;
        }

        public String getExtra() {
            return mExtra;
        }

        JSONObject toRaw() throws JSONException {
            return new JSONObject()
                    .put(KEY_TOKEN, mToken)
                    .put(KEY_STEP, mStep)
                    .put(KEY_EXTRA, mExtra);
        }
    }

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    private final Object mLock;
    private T mUploader;
    private int mMaxStepCount;
    private Future<?> mActive;

    public MaterialCollector(T uploader, int maxStep) {
        mLock = new Object();
        mUploader = uploader;
        mMaxStepCount = maxStep;
        mActive = null;
    }

    public final void active(String token) {
        active(token, null);
    }

    public final void active(String token, String extra) {
        Logger.info("[SYNC-COL-MC] Act = %s", token);
        synchronized (mLock) {
            CampaignStatus status = loadStatus();
            if (TextUtils.isEmpty(token)) {
                Logger.debug("[SYNC-COL-MC] Act @ STOP");
                setStatus(null);
            } else if (token.equals(status.getToken())) {
                Logger.debug("[SYNC-COL-MC] Act @ EXIST & Retry");
            } else {
                Logger.debug("[SYNC-COL-MC] Act @ RESET");
                status.initToken(token);
                status.setExtra(extra);
                setStatus(status);
            }
        }
        resume();
    }

    public synchronized final void resume() {
        Logger.info("[SYNC-COL-MC] Resume");
        if(null == mActive || mActive.isDone()) {
            mActive = EXECUTOR.submit(new Runnable() {
                @Override
                public void run() {
                    handleWork();
                }
            });
        } else {
            Logger.debug("[SYNC-COL-MC] Resume @ ER");
        }

    }

    protected T getUploader() {
        return mUploader;
    }

    private synchronized void handleWork() {
        CampaignStatus status = loadStatus();
        String curToken = status.getToken();

        if(!TextUtils.isEmpty(curToken)) {
            int index;
            while ((index = status.getStep()) < mMaxStepCount) {
                if (!doProcess(index, status)) {
                    Logger.error("[SYNC-COL-MC] Work FAILURE");
                    return;
                }
                synchronized (mLock) {
                    String targetToken = loadStatus().getToken();
                    if (curToken.equals(targetToken)) {
                        Logger.debug("[SYNC-COL-MC] Work OK (%d - %d) @ %s", index, mMaxStepCount, curToken);
                        status.setStep(index + 1);
                        setStatus(status);
                    } else {
                        Logger.info("[SYNC-COL-MC] Work CHANGE @ %s => %s", curToken, targetToken);
                        break;
                    }
                }
            }
        } else {
            Logger.info("[SYNC-COL-MC] Work RESET");
        }

        Logger.info("[SYNC-COL-MC] Work FINISH");
        onFinishWork(status);
        if (null == mUploader.getRequestId()) {
            synchronized (mLock) {
                String targetToken = loadStatus().getToken();
                if (curToken.equals(targetToken)) {
                    Logger.debug("[SYNC-COL-MC] Work DONE @ %s", curToken);
                    if(!TextUtils.isEmpty(targetToken)) {
                        setStatus(null);
                    }
                } else {
                    Logger.debug("[SYNC-COL-MC] Work DONE @ %s => %s", curToken, targetToken);
                }
            }
        } else {
            Logger.debug("[SYNC-COL-MC] Work SEND");
            mUploader.syncData();
        }
    }

    /**
     *
     * @param stepIndex Index >= 0
     * @param status
     * @return
     */
    protected abstract boolean doProcess(int stepIndex, CampaignStatus status);

    protected abstract void onFinishWork(CampaignStatus status);


    private CampaignStatus loadStatus() {
        return new CampaignStatus(mUploader.getData());
   }

   private void setStatus(CampaignStatus status) {
       try {
           mUploader.setData(null == status ? null : status.toRaw());
       } catch (JSONException e) {
           // SHOULD NEVER HAPPEN
           Logger.error(e);
       }
   }

}
