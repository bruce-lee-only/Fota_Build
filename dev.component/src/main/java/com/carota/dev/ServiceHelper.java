package com.carota.dev;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;

public class ServiceHelper {
    public static void startService(Context context) {
        Intent i = new Intent("ota.intent.action.BIND_CORE");
        i.setPackage(context.getPackageName());
        context.startService(i);
    }

    public static boolean bindService(Context context, ServiceConnection sc) {
        Intent i = new Intent("ota.intent.action.BIND_CORE");
        i.setPackage(context.getPackageName());
        return context.bindService(i, sc, Service.BIND_AUTO_CREATE);
    }
}
