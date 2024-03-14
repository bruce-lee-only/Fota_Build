package com.carota.hmi.policy.lock;

import com.momock.util.Logger;

public final class MyLock {
    //锁计数器
    private int lockPc = 0;

    public void resume() {
        if (!isSuspend()) return;
        synchronized (this) {
            notify();
        }
    }

    public boolean isSuspend() {
        return lockPc > 0;
    }

    public void suspend() {
        synchronized (this) {
            try {
                lockPc++;
                wait();
            } catch (InterruptedException e) {
                Logger.error(e);
                e.printStackTrace();
            }
            lockPc--;
        }
    }
}
