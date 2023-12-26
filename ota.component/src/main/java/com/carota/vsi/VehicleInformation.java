/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.vsi;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.SparseArray;

import com.carota.build.ParamVSI;
import com.carota.svr.PrivReqHelper;
import com.carota.util.ConfigHelper;
import com.carota.vehicle.IConditionHandler;
import com.carota.vehicle.VehicleService;
import com.carota.vsi.data.StatusSnapshot;
import com.carota.vsi.data.Description;
import com.carota.vsi.util.VehicleServiceHolder;
import com.momock.util.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

public class VehicleInformation {

    public static final String ATTR_TIMER_NOTIFY = "timer_notify";
    public static final String ATTR_OTA_STATE = "ota_state";
    public static final String ATTR_OTA_TASK = "ota_task";
    public static final String ATTR_OTA_PROGRESS = "ota_pg";
    public static final String ATTR_OTA_EXTRA = "ota_extra";

    private JSONObject mAttrPool;
    private Timer mTimer;
    private ParamVSI mParamVSI;
    private VehicleServiceHolder mHolder;
    private Context mContext;

    public VehicleInformation(Context context) {
        mTimer = new Timer();
        mParamVSI = ConfigHelper.get(context).get(ParamVSI.class);
        mHolder = new VehicleServiceHolder(mParamVSI.getTargetPackage(context));
        mContext = context.getApplicationContext();
    }

    public Description queryDescription() {
        Bundle data = mHolder.readProperty(mContext, VehicleService.FLAG_ID
                | VehicleService.FLAG_SPEC | VehicleService.FLAG_AREA);
        return data.size() > 0  ? new Description(data) : null;
    }

    public IConditionHandler queryCondition() {
        StatusSnapshot snapshot = new StatusSnapshot();
        for(int key : VehicleService.KEY_LIST) {
            snapshot.set(key, mHolder.queryCondition(mContext, key));
        }
        return snapshot;
    }

    public Description readDescriptionFromFile() {
        File file = new File(Logger.getLogDirPath(), "configure.prop");
        if(file.exists()) {
            try (FileInputStream fis = new FileInputStream(file)) {
                Properties props = new Properties();
                props.load(fis);
                Bundle ret = new Bundle();
                for (Map.Entry<Object, Object> e : props.entrySet()) {
                    ret.putString(e.getKey().toString(), e.getValue().toString());
                }
                if(ret.size() > 0) {
                    return new Description(ret);
                }
            } catch (Exception e) {
                Logger.error(e);
            }
        }
        return null;
    }

    public void setVehicleAttribute(String key, Object val) {
        try {
            mAttrPool.put(key, val);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public int getVehicleAttribute(String key, int def) {
        return mAttrPool.optInt(key, def);
    }

    public String getVehicleAttribute(String key, String def) {
        return mAttrPool.optString(key, def);
    }

    public void fireEvent(final String event, long delay, Bundle extra) {
        final String action = getVehicleAttribute(event, null);
        if(!TextUtils.isEmpty(action)) {
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    PrivReqHelper.doGet(action + "?evt=" + event, null);
                }
            }, delay / 10);
        }
    }
}
