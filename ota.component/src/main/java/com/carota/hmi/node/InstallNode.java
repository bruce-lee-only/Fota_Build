package com.carota.hmi.node;

import android.content.Context;
import android.os.Handler;

import com.carota.CarotaClient;
import com.carota.hmi.EventType;
import com.carota.hmi.callback.CallBackManager;
import com.carota.hmi.status.HmiStatus;
import com.momock.util.Logger;

import java.util.concurrent.ExecutionException;

class InstallNode extends BaseNode {

    private final Context context;

    InstallNode(HmiStatus hmiStatus, Handler handler, Context context, CallBackManager callback) {
        super(hmiStatus, handler, callback);
        this.context = context;
    }


    @Override
    public EventType getType() {
        return EventType.INSTALL;
    }

    @Override
    protected boolean execute() {
        try {
            if (CarotaClient.install(context, true)) {
                return true;
            }
        } catch (ExecutionException e) {
            Logger.error(e);
        }
        return false;
    }
}
