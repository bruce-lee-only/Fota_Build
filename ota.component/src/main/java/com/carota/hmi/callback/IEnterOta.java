package com.carota.hmi.callback;


import com.carota.hmi.action.EnterOtaAction;

public abstract class IEnterOta implements ICall,IStop<EnterOtaAction> {
    public abstract void onStop(boolean success, EnterOtaAction action);
}
