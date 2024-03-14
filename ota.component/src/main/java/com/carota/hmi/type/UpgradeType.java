package com.carota.hmi.type;

public enum UpgradeType {
    //0:普通升级流程,1:工厂模式,2:远控预约升级，3:远控立即升级
    DEFULT(0), FACTORY(1), SCHEDULE(2), PUSH_UPGRADE(3);

    private final int type;

    UpgradeType(int i) {
        this.type = i;
    }

    public int getTypeNum() {
        return type;
    }
}
