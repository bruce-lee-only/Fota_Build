package com.carota.hmi.callback;

import com.carota.hmi.EventType;
import com.carota.hmi.UpgradeType;
import com.carota.hmi.status.IStatus;

public interface IDownloadCall extends ICall {
    void onDownloading(UpgradeType upgradeType, EventType type, IStatus mStatus);

}
