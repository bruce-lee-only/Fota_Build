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

import android.os.Bundle;

import com.carota.core.IVehicleDetail;
import com.momock.util.JsonHelper;

import org.json.JSONException;
import org.json.JSONObject;

public class EcuInfo implements IVehicleDetail.IEcuDetail {
    public final String ID;
    public String swVer;
    public String hwVer;
    public String sn;
    public JSONObject mProps;

    public EcuInfo(String id) {
        ID = id;
    }

    public void setProp(Bundle data) {
        setProp(JsonHelper.parse(data));
    }

    public void setProp(JSONObject data) {
        mProps = data;
    }

    public static JSONObject toJson(EcuInfo info) throws JSONException {
        JSONObject jo = new JSONObject();
        jo.put("name", info.ID);
        jo.put("sv", null == info.swVer ? "" : info.swVer);
        jo.put("hv", null == info.hwVer ? "" : info.hwVer);
        jo.put("sn", null == info.sn ? "" : info.sn);
        jo.put("props", null != info.mProps ? info.mProps : new JSONObject());
        return jo;
    }

    public static EcuInfo fromJson(JSONObject info) throws JSONException {
        EcuInfo ret = new EcuInfo(info.getString("name"));
        ret.swVer = info.getString("sv");
        ret.hwVer = info.getString("hv");
        ret.sn = info.getString("sn");
        ret.mProps = info.getJSONObject("props");
        return ret;
    }

    @Override
    public String getName() {
        return ID;
    }

    @Override
    public String getSoftwareVer() {
        return swVer;
    }

    @Override
    public String getHardwareVer() {
        return hwVer;
    }

    @Override
    public String getSerialNumber() {
        return sn;
    }

    @Override
    public String getExtra(String key) {
        return mProps.optString(key);
    }

    @Override
    public String toString() {
        return ID
                + ", SW = " + swVer
                + ", HW = " + hwVer
                + ", SN = " + sn
                + ", EX = " + (null != mProps ? mProps.toString() : "");
    }
}
