/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.mda.remote.info;

import com.carota.protobuf.VehicleStatusInformation;
import com.carota.vehicle.IConditionHandler;

public class VehicleDesc {

    private String mVin;
    private String mModel;
    private String mBrand;

    public VehicleDesc(VehicleStatusInformation.VehicleInfoRsp info) {
        this(info.getVin(), info.getModel(), info.getBrand());
    }

    public VehicleDesc(String vin, String model, String brand) {
        mVin = null == vin ? "" : vin;
        mModel = null == model ? "" : model;
        mBrand = null == brand ? "" : brand;
    }

    public String getVin() {
        return mVin;
    }

    public String getModel() {
        return mModel;
    }

    public String getBrand() {
        return mBrand;
    }
}
