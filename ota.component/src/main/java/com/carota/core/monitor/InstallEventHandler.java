/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.core.monitor;

import com.carota.core.ClientState;
import com.carota.core.IInstallViewHandler;
import com.carota.core.ISession;
import com.carota.core.data.CoreStatus;
import com.carota.html.HtmlHelper;

public class InstallEventHandler implements InstallMonitor.IEvent {

    private IInstallViewHandler mView;
    private CoreStatus mStatus;

    public InstallEventHandler(CoreStatus status, IInstallViewHandler view) {
        mView = view;
        mStatus = status;
    }

    @Override
    public void onStart(ISession s) {
        mStatus.setUpgradeState(ClientState.UPGRADE_STATE_IDLE);
        mStatus.setUpgradeTriggered(true);
    }

    @Override
    public void onTrigger(ISession s) {
//        if(InstallProgress.TARGET_SLAVE.equals(target)) {
//            mStatus.setStatus(Instal);
//        }
    }

    @Override
    public void onProcess(ISession s, int state, int successCount) {
        if(mStatus.getFinishCount() != successCount) {
            mStatus.setFinishCount(successCount);
        }
        mStatus.setUpgradeState(state);
        mView.onInstallProgressChanged(s, state, successCount);
    }

    @Override
    public void onStop(ISession s, boolean cancel, int state) {

        if(!cancel) {
            mStatus.setUpgradeTriggered(false);
        }
        mStatus.setUpgradeState(state);
        mView.onInstallStop(s, state);
    }
}
