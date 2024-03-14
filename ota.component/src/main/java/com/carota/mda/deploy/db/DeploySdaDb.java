/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.mda.deploy.db;

import android.content.Context;

import com.carota.mda.deploy.bean.DeployEcuResult;
import com.carota.mda.deploy.bean.DeployResult;
import com.carota.util.DatabaseHolderEx;
import com.momock.util.JsonDatabase;
import com.momock.util.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.carota.mda.deploy.bean.DeployResult.*;

public class DeploySdaDb {

    /*
        数据库总表
        {
            usid:sdhoiascnasjapsojdpa123
            rollback[1,3]//需要回滚的组
            error[3]//回滚失败的组
            status:0//升级状态
            ecu["tbox","hu"]
        }
     */

    private static DeploySdaDb mInstances;
    private Context mContext;
    private JsonDatabase.Collection mMainTab;
    private final String TABLE = "tab";

    private final String KEY_ECU = "ecu";
    private final String KEY_ERROR = "error";
    private final String KEY_ROLLBACK = "rollback";
    private final String KEY_STATUS = "status";
    private final String KEY_USID = "usid";
    private static final int STATUS_IDLE = 0;

    private static final int STATUS_UPGRADE = 1;
    private static final int STATUS_UPGRADING = 2;
    private static final int STATUS_UPGRADE_OK = 3;

    private static final int STATUS_ROLLBACK = 4;
    private static final int STATUS_ROLLBACKING = 5;
    private static final int STATUS_ROLLBACK_OK = 6;
    private static final int STATUS_ROLLBACK_FAIL = 7;

    private Map<String, DeploySdaEcuDb> childDbMap;

    private DeploySdaDb() {
    }

    public synchronized void init(Context context) {
        childDbMap = new HashMap<>();
        mContext = context.getApplicationContext();
        mMainTab = DatabaseHolderEx.getDeployStatus(context, "main_tab");
        mMainTab.setCachable(true);
        JSONObject root = getTab();
        if (root == null) {
            root = new JSONObject();
            try {
                root.put(KEY_ECU, new JSONArray());
                root.put(KEY_ERROR, new JSONArray());
                root.put(KEY_ROLLBACK, new JSONArray());
                root.put(KEY_STATUS, STATUS_IDLE);
            } catch (JSONException e) {
                Logger.error(e);
            }
            setDb(root);
        }
    }

    private JSONObject getTab() {
        return mMainTab.get(TABLE);
    }

    private void setDb(JSONObject tab) {
        mMainTab.set(TABLE, tab);
    }

    public synchronized static DeploySdaDb getmInstances() {
        if (mInstances == null) {
            mInstances = new DeploySdaDb();
        }
        return mInstances;
    }

    public synchronized void clearAllTab(String usid) {
        Logger.info("SDA Main db Clear all date");
        try {
            JSONObject root = getTab();
            JSONArray tabNameArray = root.optJSONArray(KEY_ECU);
            if (tabNameArray != null && tabNameArray.length() > 0) {
                for (int i = 0; i < tabNameArray.length(); i++) {
                    DeploySdaEcuDb deploySdaEcuDb = new DeploySdaEcuDb(mContext, tabNameArray.optString(i));
                    deploySdaEcuDb.clearDb();
                }
            }
            childDbMap.clear();
            root = new JSONObject();
            root.put(KEY_ECU, new JSONArray());
            root.put(KEY_ERROR, new JSONArray());
            root.put(KEY_ROLLBACK, new JSONArray());
            root.put(KEY_STATUS, STATUS_IDLE);
            root.put(KEY_USID, usid);
            setDb(root);
        } catch (JSONException e) {
            Logger.error(e);
        }
    }

    private DeploySdaEcuDb getEcuTab(String ecu) {
        return childDbMap.get(ecu);
    }

    public synchronized void createResult(DeployResult mResult) {
        if (mResult != null) {
            JSONArray array = getTab().optJSONArray(KEY_ECU);
            if (array != null) {
                for (int i = 0; i < array.length(); i++) {
                    String ecu = array.optString(i, "");
                    DeploySdaEcuDb db = childDbMap.get(ecu);
                    mResult.addEcu(new DeployEcuResult(ecu, db.getPro(), db.getStatus()));
                }
            }
        }

    }

    public synchronized String getUsid() {
        return getTab().optString(KEY_USID);
    }

    private int getStatus() {
        int status = getTab().optInt(KEY_STATUS);
        switch (status) {
            case STATUS_IDLE:
                return IDLE;
            case STATUS_UPGRADE:
            case STATUS_UPGRADING:
                return UPGRADE;
            case STATUS_UPGRADE_OK:
                return SUCCESS;
            case STATUS_ROLLBACK:
            case STATUS_ROLLBACKING:
                return ROLLBACK;
            case STATUS_ROLLBACK_OK:
                return ERROR;
            case STATUS_ROLLBACK_FAIL:
                return FAILURE;
        }
        return UNRECOGNIZED;
    }

    public synchronized void setStatusUpgradeInit() {
        int status = getTab().optInt(KEY_STATUS);
        if (status<STATUS_UPGRADE) updataStatus(STATUS_UPGRADE);
    }

    public synchronized void setStatusUpgrading() {
        updataStatus(STATUS_UPGRADING);
    }

    private void setStatusUpgradeOk() {
        updataStatus(STATUS_UPGRADE_OK);
    }

    public synchronized List<Integer> setStatusRollbackInit(DeployResult mResult) {
        ArrayList<Integer> list = new ArrayList<>();
        JSONObject root = getTab();
        JSONArray jsonArray = root.optJSONArray(KEY_ROLLBACK);
        if (jsonArray != null && jsonArray.length()>0) {
            for (int i = 0; i < jsonArray.length(); i++) {
                list.add(jsonArray.optInt(i));
            }
            int status = getTab().optInt(KEY_STATUS);
            if (status<STATUS_ROLLBACK)updataStatus(STATUS_ROLLBACK);
            mResult.setTatolStatus(ROLLBACK);
        } else {
            mResult.setTatolStatus(SUCCESS);
            setStatusUpgradeOk();
        }
        if (childDbMap.isEmpty()) {
            JSONArray array = root.optJSONArray(KEY_ECU);
            for (int i = 0; i < array.length(); i++) {
                String name = array.optString(i);
                childDbMap.put(name, new DeploySdaEcuDb(mContext,name));
            }
        }
        return list;
    }

    public synchronized int setStatusRollbackEnd() {
        JSONObject root = getTab();
        JSONArray jsonArray = root.optJSONArray(KEY_ERROR);
        boolean success = (jsonArray == null || jsonArray.length() < 1);
        updataStatus(success ? STATUS_ROLLBACK_OK : STATUS_ROLLBACK_FAIL);
        return getStatus();
    }

    public synchronized void setStatusRollbacking() {
        updataStatus(STATUS_ROLLBACKING);
    }

    private void updataStatus(int status) {
        JSONObject root = getTab();
        try {
            root.put(KEY_STATUS, status);
        } catch (JSONException e) {
            Logger.error(e);
        }
        setDb(root);
    }

    public synchronized boolean isRollbacking() {
        int status = getTab().optInt(KEY_STATUS);
        return status > STATUS_UPGRADE_OK;
    }

    public synchronized boolean isRuning() {
        int status = getStatus();
        return status== UPGRADE ||status== ROLLBACK;
    }

    //------------------------------------子表相关-------------------------

    public synchronized void saveTaskInit(String ecu) {
        JSONObject root = getTab();
        JSONArray array = root.optJSONArray(KEY_ECU);
        childDbMap.put(ecu, new DeploySdaEcuDb(mContext, ecu));
        int status = root.optInt(KEY_STATUS);
        if (status < STATUS_UPGRADING) {
            getEcuTab(ecu).saveResultInit();
            for (int i = 0; i < array.length(); i++) {
                if (array.optString(i, "").contains(ecu)) {
                    return;
                }
            }
            array.put(ecu);
            setDb(root);
        }
    }

    public synchronized void saveTaskStart(String ecu) {
        getEcuTab(ecu).saveResultUpgrading();
    }

    public synchronized void saveTaskUpgradeEnd(String ecu, boolean success, int group) {
        getEcuTab(ecu).saveResultEnd(success);
        if (!success)addErrorGroup(group,KEY_ROLLBACK);
    }


    public synchronized void saveTaskPro(String ecu, int pro) {
        getEcuTab(ecu).savePro(pro);
    }

    public synchronized boolean isRuning(String ecu) {
        return getEcuTab(ecu).isRuning();
    }

    public synchronized void saveTaskRollbackStart(String ecu) {
        getEcuTab(ecu).saveResultRollbacking();
    }

    public synchronized void saveTaskRollbackInit(String ecu) {
        int status = getTab().optInt(KEY_STATUS);
        if (status < STATUS_ROLLBACKING) {
            getEcuTab(ecu).saveResultRollbackInit();
        }
    }

    public synchronized void saveTaskRollbackEnd(String ecu, boolean success, int group) {
        getEcuTab(ecu).saveResultRollbackEnd(success);
        if (!success)addErrorGroup(group,KEY_ERROR);
    }

    private void addErrorGroup(int group, String key) {
        JSONObject tab = getTab();
        JSONArray array = tab.optJSONArray(key);
        if (array == null) {
            array = new JSONArray();
        }
        for (int i = 0; i < array.length(); i++) {
            if (array.optInt(i, 1) == group) {
                return;
            }
        }
        array.put(group);
        try {
            tab.put(key, array);
        } catch (JSONException e) {
            Logger.error(e);
        }
        setDb(tab);
    }


    public synchronized boolean canUpgrade(String ecu, int group) {
        if (!canRun(group, KEY_ROLLBACK)) {
            return false;
        }
        return getEcuTab(ecu).needUpgrade();
    }

    private boolean canRun(int group, String key) {
        JSONObject tab = getTab();
        JSONArray array = tab.optJSONArray(key);
        if (array == null) {
            return true;
        }
        for (int i = 0; i < array.length(); i++) {
            if (array.optInt(i, 1) == group) {
                return false;
            }
        }
        return true;
    }

    public synchronized boolean canRollback(int group, String ecu) {
//        if (!canRun(group, KEY_ERROR)) {
//            return false;
//        }
        return getEcuTab(ecu).needRollback();
    }

}
