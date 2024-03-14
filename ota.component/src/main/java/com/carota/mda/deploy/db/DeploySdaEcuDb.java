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

import com.carota.util.DatabaseHolderEx;
import com.momock.util.JsonDatabase;
import com.momock.util.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import static com.carota.mda.deploy.bean.DeployResult.*;

public class DeploySdaEcuDb {
    private static final String TAB_ID = "upgradeResult";

    private static final String KEY_STATUS = "status";
    private static final String KEY_PRO = "pg";

    private static final int STATUS_IDLE = 0;
    private static final int STATUS_UPGRADING = 1;
    private static final int STATUS_UPGRADE_OK = 2;
    private static final int STATUS_UPGRADE_FAIL = 3;

    private static final int STATUS_ROLLBACK = 4;
    private static final int STATUS_ROLLBACKING = 5;
    private static final int STATUS_ROLLBACK_OK = 6;
    private static final int STATUS_ROLLBACK_FAIL = 7;

    private JsonDatabase.Collection slaveDA;

    /*
        新的数据表结构
        {
                status："0（默认，等待升级），1（升级中），2（升级成功），3（升级失败），
                4（等待回滚），5（回滚中），6（回滚成功），7（回滚失败）"
                pg：10
        }
     */

    DeploySdaEcuDb(Context mContext, String tabName) {
        slaveDA = DatabaseHolderEx.getDeployStatus(mContext, tabName);
        slaveDA.setCachable(true);

    }

    /**
     * 获取表
     */
    private JSONObject getTab() {
        JSONObject object = slaveDA.get(TAB_ID);
        return object==null?new JSONObject():object;
    }

    /**
     * 保存表
     */
    private void setTab(JSONObject root) {
        slaveDA.set(TAB_ID, root);
    }


    void clearDb() {
        setTab(null);
    }


    /**
     * 保存状态
     * @param status
     */
    private void saveStatus(int status) {
        JSONObject root = getTab();
        try {
            if (status > root.optInt(KEY_STATUS))root.put(KEY_STATUS, status);
        } catch (JSONException e) {
            Logger.error(e);
        }
        setTab(root);
    }

    void savePro(int pro) {
        JSONObject root = getTab();
        try {
            root.put(KEY_PRO, pro);
        } catch (JSONException e) {
            Logger.error(e);
        }
        setTab(root);
    }



    public int getPro() {
        return getTab().optInt(KEY_PRO, 0);
    }

    public int getStatus() {
        int status = getTab().optInt(KEY_STATUS, STATUS_IDLE);
        switch (status) {
            case STATUS_IDLE:
                return IDLE;
                case STATUS_UPGRADING:
                return UPGRADE;
                case STATUS_UPGRADE_OK:
                return SUCCESS;
                case STATUS_UPGRADE_FAIL:
                case STATUS_ROLLBACK:
                case STATUS_ROLLBACKING:
                    return ROLLBACK;
                case STATUS_ROLLBACK_OK:
                return FAILURE;
                case STATUS_ROLLBACK_FAIL:
                return ERROR;

        }
        return IDLE;
    }

    public boolean isRuning() {
        int status = getTab().optInt(KEY_STATUS, 0);
        return status==STATUS_ROLLBACKING || status==STATUS_UPGRADING;
    }

    public void saveResultInit() {
        saveStatus(STATUS_IDLE);
    }

    void saveResultUpgrading() {
        saveStatus(STATUS_UPGRADING);
    }

    public void saveResultRollbackInit() {
        saveStatus(STATUS_ROLLBACK);
    }

    public void saveResultEnd(boolean success) {
        saveStatus(success?STATUS_UPGRADE_OK:STATUS_UPGRADE_FAIL);
    }

    public void saveResultRollbackEnd(boolean success) {
        saveStatus(success?STATUS_ROLLBACK_OK:STATUS_ROLLBACK_FAIL);
    }

    public void saveResultRollbacking() {
        saveStatus(STATUS_ROLLBACKING);
    }

    public boolean needUpgrade() {
        int status = getTab().optInt(KEY_STATUS, STATUS_IDLE);
        return status < STATUS_UPGRADE_OK;
    }

    public boolean needRollback() {
        int status = getTab().optInt(KEY_STATUS, STATUS_IDLE);
        return status < STATUS_ROLLBACK_OK;
    }
}
