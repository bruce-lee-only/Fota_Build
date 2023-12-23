/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.mda.data;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

@SuppressLint("ApplySharedPref")
public class MasterStatus {

    private static final String SP = "mda.status";
    private static final String KEY_USID = "USID";
    private static final String KEY_PACKAGE = "PACKAGE";
    private static final String KEY_MASTER = "MASTER";
    private static final String KEY_SLAVE = "SLAVE";

    private SharedPreferences mSP;
    public MasterStatus(Context context) {
        mSP = context.getSharedPreferences(SP, Context.MODE_PRIVATE);
    }

    public void reset() {
        mSP.edit().clear().commit();
    }

    public boolean getPackage() {
        return mSP.getBoolean(KEY_PACKAGE, false);
    }

    public boolean getUpgradeSlave() {
        return mSP.getBoolean(KEY_SLAVE, false);
    }

    public boolean getUpgradeMaster() {
        return mSP.getBoolean(KEY_MASTER, false);
    }

    public String getUSID() {
        return mSP.getString(KEY_USID, "");
    }

    public void setPackage(boolean ready) {
        mSP.edit().putBoolean(KEY_PACKAGE, ready).commit();
    }

    public void setUpgrade(boolean master, boolean slave) {
        mSP.edit().putBoolean(KEY_MASTER, master).putBoolean(KEY_SLAVE, slave).commit();
    }

    public void setUSID(String usid) {
        mSP.edit().putString(KEY_USID, usid).commit();
    }
}
