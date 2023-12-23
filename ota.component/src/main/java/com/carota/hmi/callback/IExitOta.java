package com.carota.hmi.callback;

public abstract class IExitOta implements ICall {
    public abstract void onStop(boolean success);

    public abstract void onResult(boolean success);
}
