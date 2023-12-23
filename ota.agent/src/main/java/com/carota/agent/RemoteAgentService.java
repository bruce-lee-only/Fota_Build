/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.agent;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class RemoteAgentService extends Service {

    private static class AgentServiceBinder extends IRemoteAgentService.Stub {

        private Map<String, IBinder> mAgents = new HashMap<>();

        private void addRemoteAgent(String name, IBinder agent) {
            mAgents.put(name, agent);
        }

        @Override
        public int getVersionCode() throws RemoteException {
            return BuildConfig.VERSION_CODE;
        }

        @Override
        public String[] listAgentName() throws RemoteException {
            return mAgents.keySet().toArray(new String[0]);
        }

        @Override
        public IBinder[] listAgent() throws RemoteException {
            return mAgents.values().toArray(new IBinder[0]);
        }

        @Override
        public IBinder findAgentByName(String name) throws RemoteException {
            return mAgents.get(name);
        }
    }

    private final static AgentServiceBinder mServiceInterface = new AgentServiceBinder();

    @Override
    public void onCreate() {
        synchronized (mServiceInterface) {
            if(mServiceInterface.mAgents.isEmpty()) {
                initRemoteAgentService();
            }
        }

    }

    @Override
    final public IBinder onBind(Intent intent) {
        return mServiceInterface;
    }

    private void initRemoteAgentService() {
        List<RemoteAgent> adapters = new ArrayList<>();
        onAddRemoteAgent(adapters);
        if(adapters.isEmpty()) {
            throw new RuntimeException("Empty Adapter in Remote Agent Service");
        }
        for(RemoteAgent agent : adapters) {
            mServiceInterface.addRemoteAgent(agent.NAME, agent.buildBinder());
        }
    }


    /**
     * Init Available Agent list in this APK.
     * @param agents   RemoteAgentAdapter list out
     */
    protected abstract void onAddRemoteAgent(List<RemoteAgent> agents);
}
