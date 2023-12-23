package com.carota.hmi.callback;

import com.carota.hmi.action.BaseAction;

public interface IStop<T extends BaseAction> {
    void onStop(boolean success, T t);
}
