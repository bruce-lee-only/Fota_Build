package com.carota.lib.status.pump

abstract class PumpBase {

    internal abstract fun unBindOcean(className: String, tag: String)

    internal abstract fun <T> getOceanData(obj: PumpValueObject<T>): T


    internal open fun unBindObserver(className: String, tag: String) {
        unBindOcean(className, tag)
    }

    internal open fun <T> getData(obj: PumpValueObject<T>): T{
        return getOceanData(obj)
    }
}