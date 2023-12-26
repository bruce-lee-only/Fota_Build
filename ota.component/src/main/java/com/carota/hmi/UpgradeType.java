package com.carota.hmi;

public enum UpgradeType {
    //普通升级流程,工厂模式,远控预约升级，远控立即升级
    DEFULT(0), FACTORY(1), SCHEDULE(2), UPGRADE_NOW(3);

    private final int type;

    UpgradeType(int i) {
        this.type = i;
    }

    public int getTypeNum() {
        return type;
    }
}
