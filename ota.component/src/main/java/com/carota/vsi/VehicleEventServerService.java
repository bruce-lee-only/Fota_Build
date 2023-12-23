package com.carota.vsi;

import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import com.carota.CarotaVehicle;
import com.carota.OTAService;
import com.carota.core.ScheduleAttribute;
import com.carota.core.SystemAttribute;
import com.carota.vehicle.IVehicleEventService;
import com.momock.util.Logger;

public class VehicleEventServerService extends OTAService {

    private final VehicleEventServerServiceBinder mServiceInterface =
            new VehicleEventServerServiceBinder();

    /**
     * 这里需要考虑的问题还包括
     * 1.getSchedule无法查询到预约信息时如何处理
     * 2.查询到静默升级该如何处理: 直接返回0
     */
    private static class VehicleEventServerServiceBinder extends IVehicleEventService.Stub{

        @Override
        public long queryScheduleFire() throws RemoteException {
            Logger.error("VehicleEventServerService  queryScheduleFire");
            ScheduleAttribute attribute = CarotaVehicle.getSchedule();
            if (null != attribute){
                long time = 0;
                if ((int) attribute.scheduleType == ScheduleAttribute.TYPE_NORMAL) {
                    //todo: 这里需要判断是否是远控立即升级，立即升级获取当前时间
                    if(attribute.scheduleTime == 0){
                        time = System.currentTimeMillis();
                    }else {
                        time = attribute.scheduleTime;
                    }

                    //todo: 防止状态错乱导致scheduleTime是-1
                    if (time == -1) time = 0;
                }
                return time;
            }else {
                return 0;
            }
        }

        @Override
        public int queryUpgradeTime() throws RemoteException {
            Logger.error("VehicleEventServerService  queryUpgradeTime");
            return 60*60;//s
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.error("VehicleEventServerService  onCreate");
    }

    @Override
    public IBinder onBind(Intent intent) {
        Logger.error("VehicleEventServerService  onBind");
        return mServiceInterface;
    }
}
