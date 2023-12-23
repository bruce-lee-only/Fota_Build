/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.core.data;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.carota.core.ClientState;
import com.carota.core.ICoreStatus;

@SuppressLint("ApplySharedPref")
public class CoreStatus extends ClientState implements ICoreStatus {

    private static final String SP = "core.status";
    private static final String KEY_UPGRADE = "UPGRADE";
    private static final String KEY_DOWNLOAD = "DOWNLOAD";
    private static final String KEY_USID = "USID";

    private static final String KEY_TASK_FINISH = "TASK_FINISH";
    private static final String KEY_TASK_TOTAL = "TASK_TOTAL";

    private static final String KEY_IS_RESCUE = "IS_RESCUE";

    private SharedPreferences mSP;
    private int mUpgradeState;
    private int mDownloadState;

    public CoreStatus(Context context) {
        mSP = context.getSharedPreferences(SP, Context.MODE_PRIVATE);
        mUpgradeState = UPGRADE_STATE_IDLE;
        mDownloadState = DOWNLOAD_STATE_IDLE;
    }

    public void reset(String usid, int totalTaskCount) {
        mUpgradeState = UPGRADE_STATE_IDLE;
        mDownloadState = DOWNLOAD_STATE_IDLE;
        mSP.edit().clear()
                .putString(KEY_USID, usid)
                .putInt(KEY_TASK_TOTAL, totalTaskCount)
                .commit();
    }

    public void setUpgradeState(int state) {
        mUpgradeState = state;
    }

    @Override
    public int getUpgradeState() {
        return mUpgradeState;
    }

    @Override
    public boolean isUpgradeTriggered() {
        return mSP.getBoolean(KEY_UPGRADE, false);
    }

    @Override
    public void setUpgradeTriggered(boolean run) {
        mSP.edit().putBoolean(KEY_UPGRADE, run).commit();
    }

    @Override
    public void setIsRescue(boolean isRescue) {
        mSP.edit().putBoolean(KEY_IS_RESCUE, isRescue).commit();
    }

    @Override
    public boolean getIsRescue(){
        return mSP.getBoolean(KEY_IS_RESCUE, false);
    }

    @Override
    public boolean isPackageReady() {
        return mSP.getBoolean(KEY_DOWNLOAD, false);
    }

    @Override
    public int getDownloadState() {
        return mDownloadState;
    }

    public void setDownloadState(int state) {
        mDownloadState = state;
    }

    public void setPackageReady(boolean ready) {
        mSP.edit().putBoolean(KEY_DOWNLOAD, ready).commit();
    }

    @Override
    public String getUSID() {
        return mSP.getString(KEY_USID, "");
    }

    public void setFinishCount(int count) {
        mSP.edit().putInt(KEY_TASK_FINISH, count).commit();
    }

    @Override
    public int getFinishCount() {
        return mSP.getInt(KEY_TASK_FINISH, 0);
    }

    @Override
    public int getTotalCount() {
        return mSP.getInt(KEY_TASK_TOTAL, 0);
    }

}
