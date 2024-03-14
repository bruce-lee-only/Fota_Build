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

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.carota.agent.IRemoteAgent;
import com.carota.agent.IRemoteAgentCallback;
import com.carota.agent.IRemoteAgentService;
import com.momock.util.Logger;

import java.util.HashMap;
import java.util.Map;

public class RemoteAgentServiceHolder implements ServiceConnection {

    private IRemoteAgentService mRemote;
    private final String mPackageName, mAction;
    private final Map<String, IRemoteAgent> mAgentPool;
    private final IRemoteAgentCallback.Stub mCallback;

    public RemoteAgentServiceHolder(String packageName, String action, IRemoteAgentCallback.Stub callback) {
        mPackageName = packageName;
        mAction = action;
        mAgentPool = new HashMap<>();
        mCallback = callback;
    }

    @Override
    public String toString() {
        return mPackageName;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Logger.debug("RASH CONN @ " + mPackageName);
        IRemoteAgentService remote = IRemoteAgentService.Stub.asInterface(service);
        parseAgent(remote, mAgentPool);
        mRemote = remote;
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Logger.error("RASH LOST @ " + mPackageName);
        mRemote = null;
        mAgentPool.clear();
    }

    private synchronized void ensureConnected(Context context) throws Exception{
        if (null == mRemote) {
            Logger.debug("RASH ENSURE @ " + mPackageName);
            Intent i = new Intent(mAction).setPackage(mPackageName);
            if (context.bindService(i, this, Service.BIND_AUTO_CREATE)) {
                do {
                    Thread.sleep(500);
                } while (null == mRemote);
            } else {
                throw new Exception("Permission DENY @ Bind " + i.getPackage());
            }
        }
    }

    private boolean parseAgent(IRemoteAgentService svc, Map<String, IRemoteAgent> out) {
        try {
            String[] names = svc.listAgentName();
            for(String a : names) {
                IRemoteAgent agent = IRemoteAgent.Stub.asInterface(svc.findAgentByName(a));
                if(null != agent) {
                    agent.registerCallback(mCallback);
                    out.put(a, agent);
                }
            }
        } catch (Exception e) {
            Logger.error(e);
        }
        return false;
    }

    public int getVersion(Context context) {
        try {
            ensureConnected(context);
            return mRemote.getVersionCode();
        } catch (Exception e) {
            Logger.error(e);
        }
        return 0;
    }

    public IRemoteAgent getAgent(Context context, String agentName) {
        try {
            ensureConnected(context);
            return mAgentPool.get(agentName);
        } catch (Exception e) {
            Logger.error(e);
        }
        return null;
    }

    public void stop(Context context) {
        context.unbindService(this);
    }
}
