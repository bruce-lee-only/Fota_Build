package com.carota.dev.core;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.carota.InstallToast;
import com.carota.core.ISession;

public class EcusInstall extends InstallToast{
    public EcusInstall(Context base) {
        super(base);
    }

    @Override
    protected void onProgressChanged(View view, ISession s, int state, int successCount) {

    }

    @Override
    protected View onCreateView(ISession s, LayoutInflater inflater) {
        return null;
    }

    @Override
    public void onDismiss(ISession s, int state) {

    }
}
