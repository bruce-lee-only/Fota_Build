package com.carota.hmi.node;

import com.carota.CarotaClient;
import com.carota.core.IInstallViewHandler;
import com.carota.core.ISession;
import com.carota.hmi.EventType;
import com.carota.hmi.action.InstallAction;
import com.momock.util.Logger;

import java.util.concurrent.ExecutionException;

class InstallNode extends BaseNode implements IInstallViewHandler {

    private boolean inInstall;
    private boolean isResume;
    private int status;

    InstallNode(StateMachine status) {
        super(status);
    }

    @Override
    void onStart() {
        Logger.info("InstallNode onStart: [isResume]->" + isResume);
        Logger.info("InstallNode onStart: [inInstall]->" + inInstall);
        if (!isResume) mCallBack.install().onStart(mStatus.getSession());
    }

    @Override
    void onStop(boolean success) {
        mCallBack.install().onStop(success, new InstallAction(mStatus, success,isAutoRunNextNode(),mHandler), mStatus.getSession(), status);
    }


    @Override
    public EventType getType() {
        return EventType.INSTALL;
    }

    @Override
    protected boolean execute() {
        try {
            mStatus.saveInstallType();
            if (isResume || inInstall || CarotaClient.install(mStatus.getContext(), false)) {
                while (inInstall) {
                    sleep(10000);
                    Logger.debug("lipiyan update is running.....");
                    Logger.debug("lipiyan inInstall:" + inInstall );
                }
                Logger.debug("lipiyan update done");
                return true;
            }
        } catch (ExecutionException e) {
            Logger.error(e);
        }
        mStatus.clearInstallType();
        return false;
    }

    @Override
    public boolean onInstallStart(ISession s) {
        Logger.info("InstallNode onInstallStart: [isResume]->" + isResume);
        inInstall = true;
        mStatus.setSession(s);
        if (isResume) mHandler.post(() -> mCallBack.install().onStart(mStatus.getSession()));
        return true;
    }

    @Override
    public void onInstallProgressChanged(ISession s, int state, int successCount) {
        status = state;
        mStatus.setSession(s);
        if (!mStatus.isFactory())
            mHandler.post(() -> mCallBack.install().onInstallProgressChanged(s, state, successCount));
    }

    @Override
    public boolean onInstallStop(ISession s, int state) {
        Logger.debug("lipiyan**************** Install node onInstallStop");
        inInstall = false;
        mStatus.setSession(s);
        mStatus.clearInstallType();
        isResume = false;
        status = state;
        return false;
    }

    void setResume() { isResume = inInstall = true; }
}
