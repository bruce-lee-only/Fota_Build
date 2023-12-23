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

import com.carota.core.IVehicleDetail;

import java.util.ArrayList;
import java.util.List;

public class VehicleInfo implements IVehicleDetail {

    private String mVin;
    private String mModel;
    private String mBrand;
    private List<IEcuDetail> mDetails;

    public VehicleInfo(String vin, String model, String brand) {
        mVin = vin;
        mModel = model;
        mBrand = brand;
        mDetails = new ArrayList<>();
    }

    public void addEcuDetail(IEcuDetail detail) {
        mDetails.add(detail);
    }

    @Override
    public String getVinCode() {
        return mVin;
    }

    @Override
    public String getModel() {
        return mModel;
    }

    @Override
    public String getBrand() {
        return mBrand;
    }

    @Override
    public List<IEcuDetail> getEcuDetail() {
        return mDetails;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("VIN=").append(mVin)
                .append("\nMODEL=").append(mModel);
        for(IEcuDetail ed : mDetails) {
            builder.append("\n\n@ ").append(ed.toString());
        }
        return builder.toString();
    }
}
