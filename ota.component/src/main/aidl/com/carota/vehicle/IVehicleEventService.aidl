// IVehicleEventService.aidl
package com.carota.vehicle;

interface IVehicleEventService {
    long queryScheduleFire();//utc time
    int queryUpgradeTime();//s
}