/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.core.remote.info;

import com.carota.core.remote.IActionMDA;
import com.carota.mda.provider.SyncHandler;
import com.carota.protobuf.MasterDownloadAgent;

public class MDAInfo {

    public static final int FLAG_DOWNLOADING = SyncHandler.FLAG_DOWNLOADING;
    public static final int FLAG_INSTALLING = SyncHandler.FLAG_INSTALLING;
    public static final int FLAG_DOWNLOADED = SyncHandler.FLAG_DOWNLOADED;
    private final boolean isUat;

    private String mUsid;
    private int mStatus;

    public MDAInfo(MasterDownloadAgent.SyncRsp raw) {
        mUsid = raw.getUsid();
        mStatus = raw.getState();
        isUat = raw.getAction() == MasterDownloadAgent.SyncRsp.EnvmAction.UAT;
    }

    public String getUsid() {
        return mUsid;
    }

    public boolean checkStatus(int tester) {
        return (mStatus & tester) > 0;
    }

    public boolean isUat() {
        return isUat;
    }

    @Override
    public String toString() {
        return mUsid + " : " + Integer.toHexString(mStatus);
    }
}
