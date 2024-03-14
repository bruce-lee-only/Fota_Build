package com.carota.hmi.callback;

import com.carota.hmi.type.HmiTaskType;
import com.carota.hmi.type.UpgradeType;

import java.util.List;

public interface IHmiCallback {

    void updatePolicyManager(IHmiPolicyManager manager);

    void startRunPolicy(UpgradeType type);

    void startRunPolicyError(UpgradeType type);

    void taskStart(HmiTaskType taskType);

    void taskEnd(HmiTaskType taskType, IHmiResult result);

    void endRunPolicy();

    void findTimeChange(long time);

    class IHmiResult {
        private final boolean success;
        private final int installStatus;
        private final List<IHmiPolicyManager.IConditionItem> conditionResult;

        //预留
        private int error;

        public IHmiResult(boolean success) {
            this.success = success;
            this.installStatus = 0;
            this.conditionResult = null;
        }

        public IHmiResult(boolean success, int installStatus) {
            this.success = success;
            this.installStatus = installStatus;
            this.conditionResult = null;
        }

        public IHmiResult(boolean success, List<IHmiPolicyManager.IConditionItem> conditionResult) {
            this.success = success;
            this.installStatus = 0;
            this.conditionResult = conditionResult;
        }

        public boolean isSuccess() {
            return success;
        }

        public int getInstallStatus() {
            return installStatus;
        }

        public List<IHmiPolicyManager.IConditionItem> getConditionResult() {
            return conditionResult;
        }

        public int getError() {
            return error;
        }
    }

}
