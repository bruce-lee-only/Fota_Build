/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.mda.auto;

import android.text.TextUtils;

import com.carota.mda.data.UpdateCampaign;
import com.carota.mda.data.UpdateItem;
import com.carota.mda.download.IDownloadObserver;
import com.carota.mda.download.IDownloadSection;
import com.carota.mda.security.SecurityCenter;
import com.carota.mda.telemetry.FotaAnalytics;
import com.carota.mda.telemetry.FotaState;

import java.util.ArrayList;
import java.util.List;

class AutoDownloadEventHandler implements IDownloadObserver {

    private FotaAnalytics mAnalytics;
    private UpdateCampaign mSession;
    private SecurityCenter mSecure;

    AutoDownloadEventHandler(FotaAnalytics ca, UpdateCampaign session, SecurityCenter secure) {
        mAnalytics = ca;
        mSession = session;
        mSecure = secure;
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
        if (null != target && !mSession.getSecurityUrl().isEmpty()) {
            UpdateItem task = mSession.getItem(target.getIndex());
            List<String> ids = new ArrayList<>();
            ids.add(task.getProp(UpdateItem.PROP_DST_MD5));
            ids.add(task.getProp(UpdateItem.PROP_SRC_MD5));
        }
        mAnalytics.logUpgradeStateV2(usid, ecu, FotaState.OTA.STATE_DOWNLOADING, FotaState.OTA.STATE_DOWNLOADED, FotaState.OTA.DOWNLOAD.CODE_DOWNLOAD_VERIFY_MD5, "");
        mAnalytics.logUpgradeStateV2(usid, ecu, FotaState.OTA.STATE_DOWNLOADING, FotaState.OTA.STATE_DOWNLOADED, 0, "");
    }

    @Override
    public void onError(String usid, IDownloadSection target) {
        String ecu = null != target ? target.getName() : null;
        mAnalytics.logUpgradeStateV2(usid, ecu, FotaState.OTA.STATE_DOWNLOAD_FAILURE, FotaState.OTA.STATE_DOWNLOAD_FAILURE, FotaState.OTA.DOWNLOAD.CODE_DOWNLOAD_MAX_RETRY, "");
    }

    @Override
    public void onStop(String usid, boolean finished, IDownloadSection section) {
        if (null == section) return;
        if (TextUtils.isEmpty(section.getName())) return;
        if (finished) {
            mAnalytics.logUpgradeStateV2(usid, section.getName(), FotaState.OTA.STATE_DOWNLOADED, FotaState.OTA.STATE_DOWNLOADED, FotaState.OTA.DOWNLOAD.CODE_DOWNLOAD_VERIFY_MD5, "");
            mAnalytics.logUpgradeStateV2(usid, section.getName(), FotaState.OTA.STATE_DOWNLOADED, FotaState.OTA.STATE_DOWNLOADED, 0, "");
        } else {
            mAnalytics.logUpgradeStateV2(usid, section.getName(), FotaState.OTA.STATE_DOWNLOAD_FAILURE, FotaState.OTA.STATE_DOWNLOAD_FAILURE, FotaState.OTA.DOWNLOAD.CODE_DOWNLOAD_MAX_RETRY, "");
        }
    }

}
