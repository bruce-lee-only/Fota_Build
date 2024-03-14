package com.carota.hmi.task.callback;

import com.carota.core.IInstallViewHandler;
import com.carota.core.ISession;
import com.carota.hmi.type.UpgradeType;

public interface InitTaskCallback extends IInstallViewHandler {

    void updateSession(ISession session);

    void initEnd(boolean cancheck);

    void findRemoteUprade(UpgradeType type);

    void findTimeChange(long time);

}
