/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.sda.util;

import android.content.Context;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;

import com.carota.agent.IRemoteAgent;
import com.carota.agent.RemoteAgent;
import com.carota.sda.ISlaveMethod;
import com.momock.util.Logger;

import java.io.File;

public class SlaveMethod implements ISlaveMethod {

    protected final Bundle queryInfo(IRemoteAgent agent, int flag, String name, Bundle bomInfo) {
        try {
            return agent.queryInfo(flag, name, bomInfo);
        } catch (Exception e) {
            Logger.error(e);
        }
        return null;
    }

    public Bundle readInfo(Context context, IRemoteAgent agent, String ecuName, Bundle bomInfo) {
        return queryInfo(agent, RemoteAgent.FLAG_PROP
                | RemoteAgent.FLAG_SDK | RemoteAgent.FLAG_APP, ecuName, bomInfo);
    }

    @Override
    public Bundle queryStatus(Context context, IRemoteAgent agent, String ecuName) {
        return queryInfo(agent, RemoteAgent.FLAG_STATUS, ecuName, null);
    }

    @Override
    public boolean startUpgrade(Context context, File path, IRemoteAgent agent, Bundle data) {
        if(path==null || !path.exists()) {
            try {
                return RemoteAgent.INSTALL_SUCCESS == agent.triggerUpgrade(null, data);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        try (ParcelFileDescriptor pfd = ParcelFileDescriptor.open(path, ParcelFileDescriptor.MODE_READ_ONLY)){
            return RemoteAgent.INSTALL_SUCCESS == agent.triggerUpgrade(pfd, data);
        } catch (Exception e) {
            Logger.error(e);
        }
        return false;
    }

    @Override
    public boolean finishUpgrade(Bundle data, IRemoteAgent agent) {
        try {
            return agent.finishUpgrade(data);
        } catch (Exception e) {
            Logger.error(e);
        }
        return false;
    }
}
