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

import com.momock.util.Convert;

public abstract class ModuleParser extends ConfigParser {

    /**
     <node id="m" type="t">
         <name>ota_m</name>
         <addr>127.0.0.1</addr>
         <port>20003</port>
     </node>
     */

    private String mHost;
    private String mAddr;
    private int mPort;
    private String mUrlRegister;
    private String mType;

    public ModuleParser(String id) {
        super(id);
    }

    public String getHost() {
        return mHost;
    }

    public String getAddr() {
        return mAddr;
    }

    public int getPort() {
        return mPort;
    }

    final public String getType() {
        return mType;
    }

    protected void setType(String type) {
        mType = type;
    }

    @Override
    final protected void set(String tag, String name, String val, boolean enabled) {
        if(tag.equals("addr")) {
            mAddr = val;
        } else if(tag.equals("name")) {
            mHost = val;
        } else if(tag.equals("port")) {
            mPort = Convert.toInteger(val, 80);
        } else {
            setExtras(tag, name, val, enabled);
        }
    }

    protected abstract void setExtras(String tag, String name, String val, boolean enabled);
}
