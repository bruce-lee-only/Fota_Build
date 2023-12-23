/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.core;

public interface ICoreStatus {
    int getUpgradeState();

    boolean isUpgradeTriggered();

    boolean isPackageReady();

    int getDownloadState();

    String getUSID();

    int getFinishCount();

    int getTotalCount();

    boolean getIsRescue();

    void setIsRescue(boolean isRescue);

    void setUpgradeTriggered(boolean run);

}
