// IRemoteAgentCallback.aidl
package com.carota.agent;

interface IRemoteAgentCallback {

    boolean onEventProcess(String name, String type, String event, in Bundle extra);

    boolean onLogArchived(String name, String type, in Bundle extra, in ParcelFileDescriptor archive);

}
