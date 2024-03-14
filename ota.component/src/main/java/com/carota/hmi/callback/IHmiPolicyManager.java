package com.carota.hmi.callback;

import android.os.NetworkOnMainThreadException;

import com.carota.core.ISession;
import com.carota.core.VehicleCondition;
import com.carota.hmi.type.UpgradeType;

public interface IHmiPolicyManager {

    UpgradeType getUpgradeType();

    boolean startPolicy();

    /**
     * run fail task again
     */
    boolean runFailTaskAgain();

    /**
     * run next task
     * when the task is succsss
     * and the next task is HmiTaskType.wait_user_run_next
     */
    boolean runNextTaskWhenNeedUserRun();


    ISession getSession();

    int getTotalDownloadPro();

    String getTotalDownloadSpeed();

    /**
     * @param time -1 clear time
     *             >0 set Time
     * @return set result
     */
    boolean setTime(long time) throws NetworkOnMainThreadException;

    /**
     * get Condition
     *
     * @return
     */
    VehicleCondition getCondition() throws NetworkOnMainThreadException;

    //not install or timeout keep download
    boolean endPolicy();

    boolean endPolicyKeepDownload();

    void sendEvent(int type, int eventCodePreErrorOther);

    class IConditionItem {
        private final VehicleCondition.Item item;
        private final boolean success;

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
