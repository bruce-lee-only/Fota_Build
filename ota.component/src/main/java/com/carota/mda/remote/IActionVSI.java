/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.mda.remote;

import android.os.Bundle;

import com.carota.core.SystemAttribute;
import com.carota.mda.remote.info.VehicleDesc;
import java.util.List;

public interface IActionVSI extends IActionCondition{
    VehicleDesc queryInfo();
    int registerEvent(String action, String activeUri);
    boolean fireEvent(String action, long delaySec, Bundle extra);
    boolean removeEvent(String action);
	SystemAttribute setSystemAttribute(List<SystemAttribute.Configure> cfg);
}
