package com.carota.hmi;

public interface ErrorCode {
    int ERROR_DEFULT = 0;
    //正在执行Node,不允许再次执行
    int ERROR_NODE_RUNNING = 1;
    //UpgradeType 不一样，禁止执行
    int ERROR_UPGRADE_TYPE_DIFFERENT = 2;

    //找不到IRemote,程序错误
    int ERROR_NOT_FIND_REMOTE = 3;

    int ERROR_NODE_TYPE_IS_NULL = 4;
    int ERROR_NODE_AUTO_RUNNING = 5;
    int ERROR_NODE_IS_NULL = 6;
    int ERROR_FACTORY_NODE_NOT_RUN = 7;
    int ERROR_NODE_DIEABLE_RUN = 8;
}
