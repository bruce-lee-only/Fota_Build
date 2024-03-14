package com.carota.dm.down;

public interface IDownCallback {
    void progress(final int speed, final long length, final long fileLength);
}
