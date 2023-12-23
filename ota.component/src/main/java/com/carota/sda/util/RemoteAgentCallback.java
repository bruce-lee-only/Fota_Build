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

import com.carota.agent.IRemoteAgentCallback;
import com.momock.util.FileHelper;
import com.momock.util.Logger;

import java.io.File;

public class RemoteAgentCallback extends IRemoteAgentCallback.Stub {

    private Context mContext;

    public static File getLogCacheDir(Context context, String name) {
        if(null == name) {
            return new File(context.getFilesDir(), "logs");
        } else {
            return new File(context.getFilesDir(), "logs/" + name);
        }
    }

    public RemoteAgentCallback(Context context) {
        mContext = context.getApplicationContext();
    }

    @Override
    public boolean onEventProcess(String name, String type, String event, Bundle extra) throws RemoteException {
        SlaveEvent se = SlaveEvent.getCache(mContext, name);
        return null != se && se.logEvent(type, event, extra);
    }

    @Override
    public boolean onLogArchived(String name, String type, Bundle extra, ParcelFileDescriptor archive) throws RemoteException {
        File dir = getLogCacheDir(mContext, name);
        FileHelper.cleanDir(dir);
        FileHelper.mkdir(dir);
        File file = new File(dir,  name + "_" + type + ".zip");
        try {
            ParcelFileDescriptor.AutoCloseInputStream aci = new ParcelFileDescriptor.AutoCloseInputStream(archive);
            FileHelper.copy(aci, file);
            aci.close();
            return true;
        } catch (Exception e) {
            Logger.error(e);
        }
        return false;
    }
}
