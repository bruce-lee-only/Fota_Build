package com.carota.dm.down;

public interface IFileDownloader {
    int CODE_NONE = 0;
    int CODE_SUCCESS = 1;
    int CODE_NO_LENGTH = 2;
    int CODE_MAX_RETRY = 3;
    int CODE_RUNING = 4;
    int CODE_ERROE = 5;
    int CODE_CANCLE = 6;

    int start();

    void stop();

    boolean isRun();
}
