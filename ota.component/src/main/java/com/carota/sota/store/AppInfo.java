/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/
package com.carota.sota.store;

public class AppInfo {

    private String mPkgName;
    private int mVerCode;
    private String mVerName;

    AppInfo(String packageName, int versionCode, String versionName) {
        mPkgName = packageName;
        mVerCode = versionCode;
        mVerName = versionName;
    }

    public String getPackageName() {
        return mPkgName;
    }

    public int getVersionCode() {
        return mVerCode;
    }

    public String getVersionName() {
        return mVerName;
    }
}
