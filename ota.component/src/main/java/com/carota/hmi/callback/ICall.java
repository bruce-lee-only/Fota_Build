package com.carota.hmi.callback;

import com.carota.hmi.UpgradeType;
import com.carota.hmi.status.IStatus;

public interface ICall {
    void onStart(UpgradeType upgradeType);

    void onError(UpgradeType upgradeType, int error);

    void onEnd(UpgradeType upgradeType, boolean success, IStatus status);
}
