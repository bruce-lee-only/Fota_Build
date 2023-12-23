/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.rescue;

import android.content.Intent;

import com.carota.OTAService;
import com.carota.util.SerialExecutor;

public class RescueDaemonService extends OTAService implements Runnable{

    @Override
    public void onCreate() {
        super.onCreate();
        new SerialExecutor().execute(this);
    }

    @Override
    public void run() {
        synchronized (RescueDaemonService.class) {
            RescueCarotaClient.bootStrap(this);
        }
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }
}
