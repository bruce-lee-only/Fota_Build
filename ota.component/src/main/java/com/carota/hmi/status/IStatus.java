package com.carota.hmi.status;

import com.carota.core.ClientState;
import com.carota.core.ISession;
import com.carota.core.VehicleCondition;

import java.util.List;

public interface IStatus {

    public static final int UPGRADE_STATE_IDLE = ClientState.UPGRADE_STATE_IDLE;
    public static final int UPGRADE_STATE_UPGRADE = ClientState.UPGRADE_STATE_UPGRADE;
    public static final int UPGRADE_STATE_SUCCESS = ClientState.UPGRADE_STATE_SUCCESS;
    public static final int UPGRADE_STATE_ROLLBACK = ClientState.UPGRADE_STATE_ROLLBACK;
    public static final int UPGRADE_STATE_ERROR = ClientState.UPGRADE_STATE_ERROR;
    public static final int UPGRADE_STATE_FAILURE = ClientState.UPGRADE_STATE_FAILURE;

    /**
     * upgrade task info
     *
     * @return ISession
     */
    ISession getSession();

    boolean canDownload();

    //下载进度信息

    /**
     * download progress
     *
     * @return 0-100
     */
    int getDownloadPro();

    /**
     * download speed
     *
     * @return MB/S or KB/S
     */
    String getDownloadSpeed();

    /**
     * downresult
     *
     * @return
     */
    boolean downloadResult();

    /**
     * Upgreade Status
     *
     * @return
     */
    int getUpgradeStatus();

    /**
     * Upgrade success ecu nums
     *
     * @return success ecu num
     */
    int getEcuUpgradeSuccessCount();

    /**
     * The Car Condition
     *
     * @return
     */
    List<IConditionItem> getCondition();

    public static class IConditionItem {
        public VehicleCondition.Item item;
        public boolean success;

        public VehicleCondition.Item getItem() {
            return item;
        }

        public boolean isSuccess() {
            return success;
        }

        public IConditionItem(VehicleCondition.Item item, boolean success) {
            this.item = item;
            this.success = success;
        }
    }

}
