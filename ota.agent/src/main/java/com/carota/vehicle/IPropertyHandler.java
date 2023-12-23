/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.vehicle;

import java.util.Map;

public interface IPropertyHandler {

    String getVinCode();

    String getModel();

    String getBrand();

    Map<String, String> getExtra(int flag);

}
