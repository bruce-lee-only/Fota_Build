/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.mda.deploy;

public interface IDeploySafety {
    boolean ensureSafety(long timeout,String name);
    void openVehicleSilent();
    boolean switchVehicle(String name);
    void onInstallEnd();
    boolean onEcuInstallEnd(String name);
    boolean onVerifyPrecondition(long timeout, long interval);
}
