package com.carota.hmi.db;

import android.content.Context;
import android.content.SharedPreferences;

import com.carota.hmi.type.UpgradeType;

public class HmiDbManager {
    private final static String NAME = "carota.hmi";
    private final static String KEY_INSTALL_POLICY_TYPE = "type";
    private final SharedPreferences preferences;

    public HmiDbManager(Context context) {
        preferences = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
    }

    public void setInstallPolicyType(UpgradeType type) {
        preferences.edit()
                .putInt(KEY_INSTALL_POLICY_TYPE, type.getTypeNum())
                .commit();
    }

    public UpgradeType getInstallPolicyType() {
        int type = preferences.getInt(KEY_INSTALL_POLICY_TYPE, 0);
        switch (type) {
            case 1:
                return UpgradeType.FACTORY;
            case 2:
                return UpgradeType.SCHEDULE;
            case 3:
                return UpgradeType.PUSH_UPGRADE;
            default:
                return UpgradeType.DEFULT;
        }
    }
}
