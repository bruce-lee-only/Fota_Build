/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.build;

public abstract class ConfigParser {

    public final String ID;
    private boolean mEnabled;
    private ConfigHook mCfgHook;

    public ConfigParser(String id) {
        ID = id;
        mEnabled = false;
    }

    public boolean isEnabled() {
        return mEnabled;
    }

    protected abstract void set(String tag, String name, String val, boolean enabled);

    protected void setType(String type) {

    }

    ConfigParser setEnabled(boolean enabled) {
        mEnabled = enabled;
        return this;
    }

    void setConfigHook(ConfigHook hook) {
        mCfgHook = hook;
    }

    protected String mockUrl(String path) {
        return mCfgHook.mockUrl(path);
    }
}
