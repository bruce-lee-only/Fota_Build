package com.carota.vehicle;

interface IVehicleService {

    Bundle readProperty(int flag);

    int queryCondition(int id, int def);
}
