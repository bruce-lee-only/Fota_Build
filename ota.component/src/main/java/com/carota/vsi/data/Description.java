/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.vsi.data;

import android.os.Bundle;

public class Description {

    private Bundle mRaw;

    public Description(Bundle data) {
        mRaw = data;
    }

    public String getVinCode() {
        return mRaw.getString("vin", "");
    }

    public String getModel() {
        return mRaw.getString("model", "");
    }

    public String getBrand() {
        return mRaw.getString("brand", "");
    }

    public String getExtra(String key) {
        return mRaw.getString(key);
    }
}
