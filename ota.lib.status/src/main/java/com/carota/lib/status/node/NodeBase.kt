package com.carota.lib.status.node

import com.carota.lib.common.uitls.LiveDataUtil

abstract class NodeBase {

    internal abstract fun <T>bindOcean(className: String, obj: NodeValueObject<T>)

    internal abstract fun unBindOcean(className: String, tag: String)

    internal abstract fun <T> getOceanData(obj: NodeValueObject<T>): T

    internal open fun <T> bindObserver(className: String, obj: NodeValueObject<T>) {
        bindOcean(className, obj)
    }

    internal open fun unBindObserver(className: String, tag: String) {
        unBindOcean(className, tag)
    }

    internal open fun <T> getData(obj: NodeValueObject<T>): T{
        return getOceanData(obj)
    }
}