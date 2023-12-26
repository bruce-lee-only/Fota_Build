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

import java.util.List;

public interface IVehicleDetail {

    interface IEcuDetail {
        String getName();
        String getSoftwareVer();
        String getHardwareVer();
        String getSerialNumber();
        String getExtra(String key);
    }

    String getVinCode();

    String getModel();

    String getBrand();

    List<IEcuDetail> getEcuDetail();
}
