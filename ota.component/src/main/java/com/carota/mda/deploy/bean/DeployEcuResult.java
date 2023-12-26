package com.carota.mda.deploy.bean;

/**
 * 每个ECU升级的状态
 */
public class DeployEcuResult {
    final String name;
    int pro;
    int status;

    public DeployEcuResult(String name, int pro, int status) {
        this.name = name;
        this.pro = pro;
        this.status = status;
    }

    @Override
    public String toString() {
        return "EcuResult{" +
                "name='" + name + '\'' +
                ", pro=" + pro +
                ", status=" + status +
                '}';
    }

    public String getName() {
        return name;
    }

    public int getPro() {
        return pro;
    }

    public int getStatus() {
        return status;
    }
}
