/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import com.carota.core.ClientState;
import com.carota.core.IInstallViewHandler;
import com.carota.core.ISession;

public abstract class InstallToast extends ContextWrapper implements IInstallViewHandler {

    private View mRoot;
    private final WindowManager mWindowManager;
    private boolean mIsShowing;
    private static final Handler sHandler = new Handler(Looper.getMainLooper());

    public InstallToast(Context base) {
        super(base.getApplicationContext());
        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        mIsShowing = false;
    }

    @Override
    public final boolean onInstallStart(ISession s) {
        if(!mIsShowing) {
            mRoot = onCreateView(s, LayoutInflater.from(this));
            if (null != mRoot) {
                mIsShowing = true;
                final WindowManager.LayoutParams params = new WindowManager.LayoutParams();
                onShow(params);
                sHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mWindowManager.addView(mRoot, params);
                    }
                });
                return true;
            }
        }
        return false;
    }

    @Override
    public final void onInstallProgressChanged(final ISession s, final int state, final int successCount) {
        sHandler.post(new Runnable() {
            @Override
            public void run() {
                if(mIsShowing && null != s && ClientState.UPGRADE_STATE_IDLE != state) {
                    onProgressChanged(mRoot, s, state, successCount);
                }
            }
        });
    }

    @Override
    public final boolean onInstallStop(ISession s, int status) {
        if(mIsShowing) {
            onDismiss(s, status);
            mIsShowing = false;
            sHandler.post(new Runnable() {
                @Override
                public void run() {
                    mWindowManager.removeViewImmediate(mRoot);
                }
            });
            return true;
        }
        return false;
    }

    protected abstract void onProgressChanged(View view, ISession s, int state, int successCount);

    protected abstract View onCreateView(ISession s, LayoutInflater inflater);

    public void onShow(WindowManager.LayoutParams params) {
        params.gravity = Gravity.FILL_HORIZONTAL | Gravity.CENTER_VERTICAL;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.format = PixelFormat.TRANSLUCENT;
        // params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        //        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        params.windowAnimations = android.R.style.Animation_Dialog;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            params.type = WindowManager.LayoutParams.TYPE_TOAST;
        }
    }

    public abstract void onDismiss(ISession s, int state);

}
