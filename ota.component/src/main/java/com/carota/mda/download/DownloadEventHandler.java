/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.mda.download;

import android.text.TextUtils;

import com.carota.mda.data.MasterStatus;
import com.carota.mda.data.UpdateCampaign;
import com.carota.mda.data.UpdateItem;
import com.carota.mda.security.SecurityCenter;
import com.carota.mda.telemetry.FotaAnalytics;
import com.carota.mda.telemetry.FotaState;

import java.util.ArrayList;
import java.util.List;

public class DownloadEventHandler implements IDownloadObserver {

    private final FotaAnalytics mAnalytics;
    private final MasterStatus mStatus;

    public DownloadEventHandler(FotaAnalytics ca, MasterStatus ms) {
        mAnalytics = ca;
        mStatus = ms;
    }

    @Override
    public void onStart(String usid) {

    }

    @Override
    public void onDownload(String usid, IDownloadSection target) {
        String ecu = null != target ? target.getName() : null;
        mAnalytics.logUpgradeStateV2(usid, ecu, FotaState.OTA.STATE_DOWNLOADING, FotaState.OTA.STATE_DOWNLOADING, 0, "");
    }

    @Override
    public void onDownloading(String usid, IDownloadSection target) {

    }

    @Override
    public void onDownloaded(String usid, IDownloadSection target) {
        String ecu = null != target ? target.getName() : null;
        mAnalytics.logUpgradeStateV2(usid, ecu, FotaState.OTA.STATE_DOWNLOADING, FotaState.OTA.STATE_DOWNLOADED, FotaState.OTA.DOWNLOAD.CODE_DOWNLOAD_VERIFY_MD5, "");
        mAnalytics.logUpgradeStateV2(usid, ecu, FotaState.OTA.STATE_DOWNLOADING, FotaState.OTA.STATE_DOWNLOADED, 0, "");
    }

    @Override
    public void onError(String usid, IDownloadSection target) {
        if (null == target) return;
        String ecu = target.getName();
        if (TextUtils.isEmpty(ecu)) return;
        mAnalytics.logUpgradeStateV2(usid, ecu, FotaState.OTA.STATE_DOWNLOAD_FAILURE, FotaState.OTA.STATE_DOWNLOAD_FAILURE, FotaState.OTA.DOWNLOAD.CODE_DOWNLOAD_MAX_RETRY, "");
    }

    @Override
    public void onStop(String usid, boolean finished, IDownloadSection target) {
        mStatus.setPackage(finished);
        if (null == target) return;
        if (TextUtils.isEmpty(target.getName())) return;
        if (finished) {
            mAnalytics.logUpgradeStateV2(usid, target.getName(), FotaState.OTA.STATE_DOWNLOADED, FotaState.OTA.STATE_DOWNLOADED, FotaState.OTA.DOWNLOAD.CODE_DOWNLOAD_VERIFY_MD5, "");
            mAnalytics.logUpgradeStateV2(usid, target.getName(), FotaState.OTA.STATE_DOWNLOADED, FotaState.OTA.STATE_DOWNLOADED, 0, "");
        } else {
            mAnalytics.logUpgradeStateV2(usid, target.getName(), FotaState.OTA.STATE_DOWNLOAD_FAILURE, FotaState.OTA.STATE_DOWNLOAD_FAILURE, FotaState.OTA.DOWNLOAD.CODE_DOWNLOAD_MAX_RETRY, "");
        }
    }
}
