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

import android.content.Intent;
import com.carota.util.SerialExecutor;

public class DaemonService extends OTAService implements Runnable{

    @Override
    public void onCreate() {
        super.onCreate();
        new SerialExecutor().execute(this);
    }

    @Override
    public void run() {
        synchronized (DaemonService.class) {
            CarotaClient.bootStrap(this);
        }
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }
}
