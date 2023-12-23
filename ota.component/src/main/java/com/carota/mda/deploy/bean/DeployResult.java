package com.carota.mda.deploy.bean;


import java.util.LinkedHashMap;
import java.util.Objects;

public class DeployResult {
    public static final  int UNRECOGNIZED=-1;
    private final String usid;

    /**
     * <code>IDLE = 0;</code>
     */
    public static final int IDLE = 0;
    /**
     * <code>UPGRADE = 1;</code>
     */
    public static final int UPGRADE = 1;
    /**
     * <code>SUCCESS = 2;</code>
     */
    public static final int SUCCESS = 2;
    /**
     * <code>ROLLBACK = 3;</code>
     */
    public static final int ROLLBACK = 3;
    /**
     * <pre>
     *回滚成功
     * </pre>
     *
     * <code>ERROR = 4;</code>
     */
    public static final int ERROR = 4;
    /**
     * <pre>
     *回滚失败
     * </pre>
     *
     * <code>FAILURE = 5;</code>
     */
    public static final int FAILURE = 5;
    /**
     * <pre>
     *wait rollback
     * </pre>
     *
     * <code>WAIT = 6;</code>
     */
    public static final int WAIT = 6;
    //升级总状态
    private int mStatus;

    private final LinkedHashMap<String , DeployEcuResult> mEcuResultMap;
    private boolean isBlock;

    public DeployResult(String usid) {
        this.usid = usid;
        this.mStatus =IDLE;
        mEcuResultMap = new LinkedHashMap<>();
    }

    public void setTatolStatus(int status) {
        this.mStatus = status;
    }

    public void addEcu(DeployEcuResult task) {
        mEcuResultMap.put(task.name, task);
    }

    /**
     * 更新Ecu升级进度
     * @param ecu
     * @param pro
     */
    public void updateEcuPro(String ecu,int pro) {
        Objects.requireNonNull(mEcuResultMap.get(ecu)).pro = pro;
    }

    public void updateEcuUpgrading(String ecu) {
        Objects.requireNonNull(mEcuResultMap.get(ecu)).status = UPGRADE;
    }

    public void updateEcuUpgradEnd(String ecu,boolean success) {
        Objects.requireNonNull(mEcuResultMap.get(ecu)).status = success?SUCCESS :WAIT;
    }

    public void updateEcuRollbacking(String ecu) {
        Objects.requireNonNull(mEcuResultMap.get(ecu)).status = ROLLBACK;
    }

    public void updateEcuRollbackEnd(String ecu,boolean success) {
        Objects.requireNonNull(mEcuResultMap.get(ecu)).status = success?ERROR :FAILURE;
    }

    @Override
    public String toString() {
        return "{" +
                "usid='" + usid + '\'' +
                ", mStatus=" + mStatus +
                ", mEcuResultMap=" + mEcuResultMap +
                '}';
    }

    public LinkedHashMap<String, DeployEcuResult> getmEcuResultMap() {
        return mEcuResultMap;
    }

    public String getUsid() {
        return usid;
    }

    public int getmStatus() {
        return mStatus;
    }

    public void setBlock(boolean isBlock) {
        this.isBlock = isBlock;
    }
}
