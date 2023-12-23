/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.dtc.log.engine;

import android.text.TextUtils;

public class KeywordRule extends Rule {

    private String mData;

    public KeywordRule(int target, String key) {
        super(target);
        mData = key;
    }

    @Override
    protected boolean onMatch(String data) {
        return !TextUtils.isEmpty(data) && data.contains(mData);
    }
}
