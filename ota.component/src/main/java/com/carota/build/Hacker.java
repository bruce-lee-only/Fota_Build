/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.build;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.carota.ModeReceiver;
import com.momock.util.Logger;
import com.momock.util.SystemHelper;

public class Hacker extends ConfigHook {

    private static boolean MODE_MARK_DEF = false;
    private ParamLocal mParamLocal;
    private Context mContext;
    private boolean mModeTestEnabled;
    private ComponentName mModeMark;

    public Hacker(Context ctx) {
        mParamLocal = null;
        mContext = ctx.getApplicationContext();
        mModeMark = new ComponentName(mContext, ModeReceiver.class);
        mModeTestEnabled = SystemHelper.isComponentEnabled(mContext, mModeMark, MODE_MARK_DEF);
    }

    public Hacker load(ParamLocal param) {
        mParamLocal = param;
        return this;
    }

    public void setTestModeEnabled(boolean enable) {
        if(mModeTestEnabled != enable) {
            SystemHelper.setComponentEnabled(mContext, mModeMark, MODE_MARK_DEF, enable);
        }
        // clear data
//        SystemHelper.execScript("pm clear " + mContext.getPackageName());
    }

    public void showTestModeHint() {
        mContext.sendBroadcast(new Intent(mContext, ModeReceiver.class));
    }

    @Override
    public boolean isTestModeEnabled() {
        return mModeTestEnabled;
    }

    @Override
    public String mockUrl(String path) {
        if(null == path || path.startsWith("http")) {
           return path;
        }

        Logger.check(path.startsWith("/"), "RTE ERROR @ PATH");

        if(isTestModeEnabled()) {
            return mParamLocal.getTestBaseUrl() + path;
        } else {
            return mParamLocal.getProductionBaseUrl() + path;
        }
    }
}
