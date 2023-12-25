package com.carota.hmi.callback;

import com.carota.hmi.UpgradeType;
import com.carota.hmi.status.IStatus;

public interface IInstallCall extends ICall {
    void onInstallProgressChanged(UpgradeType upgradeType, IStatus status);
}
