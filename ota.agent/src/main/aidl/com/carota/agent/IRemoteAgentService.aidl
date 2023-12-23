// SlaveDownloadAgentPool.aidl
package com.carota.agent;

interface IRemoteAgentService {

    int getVersionCode();

    String[] listAgentName();

    IBinder[] listAgent();

    IBinder findAgentByName(String name);

}
