/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.vsi.util;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

import com.carota.vehicle.IConditionHandler;
import com.carota.vehicle.IVehicleService;
import com.momock.util.Logger;

public class VehicleServiceHolder implements ServiceConnection {

    private final String mTargetPackage;
    private IVehicleService mRemote;

    public VehicleServiceHolder(String pkgName) {
        mTargetPackage = pkgName;
        mRemote = null;
    }


    public Bundle readProperty(Context ctx, int flag) {
        try {
            ensureConnected(ctx);
            return mRemote.readProperty(flag);
        } catch (Exception e) {
            Logger.error(e);
        }
        return new Bundle();
    }

    public int queryCondition(Context ctx, int id) {
        try {
            ensureConnected(ctx);
            return mRemote.queryCondition(id, IConditionHandler.STATE_UNKNOWN);
        } catch (Exception e) {
            Logger.error(e);
        }
        return IConditionHandler.STATE_UNKNOWN;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mRemote = IVehicleService.Stub.asInterface(service);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mRemote = null;
    }

    private void ensureConnected(Context context) throws InterruptedException {
        if (null == mRemote) {
            Context ctx = context.getApplicationContext();
            Logger.debug("VSM ENSURE @ " + mTargetPackage);
            Intent i = new Intent("ota.intent.action.BIND_VS")
                    .setPackage(mTargetPackage);
            if (ctx.bindService(i, this, Service.BIND_AUTO_CREATE)) {
                do {
                    Thread.sleep(500);
                } while (null == mRemote);
            } else {
                throw new RuntimeException("VSM Permission DENY @ Bind " + i.getPackage());
            }
        }
    }
}
