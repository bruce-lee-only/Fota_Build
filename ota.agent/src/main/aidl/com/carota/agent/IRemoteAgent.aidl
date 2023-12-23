// SlaveDownloadAgentPool.aidl
package com.carota.agent;
import com.carota.agent.IRemoteAgentCallback;

interface IRemoteAgent {

    boolean registerCallback(in IRemoteAgentCallback callback);

    Bundle queryInfo(int flag, String name, in Bundle extra);

    int triggerUpgrade(in ParcelFileDescriptor data, in Bundle extra);

    boolean archiveLogs(String type, in Bundle extra);

    boolean finishUpgrade(in Bundle extra);
}
