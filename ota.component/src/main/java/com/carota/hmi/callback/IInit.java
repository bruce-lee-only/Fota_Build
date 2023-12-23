package com.carota.hmi.callback;

import com.carota.hmi.action.InitAction;

public abstract class IInit implements ICall {
    public abstract void onStop(Boolean success, InitAction action);

    public abstract void onCanCheck(Boolean can);
}
