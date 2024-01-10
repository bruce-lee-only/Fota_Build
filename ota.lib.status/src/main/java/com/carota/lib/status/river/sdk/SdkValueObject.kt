package com.carota.lib.status.river.sdk

import com.carota.lib.common.uitls.Logger
import kotlin.properties.Delegates

class SdkValueObject<T>(
    //todo: 默认值
    default:T,
    //todo: 当前对象的tag
    val tag:String,
    //todo: 是否需要进行存储
    private val needSave: Boolean = true,
    //todo: 是否允许监听
    private val watchable: Boolean = true,
    //todo: 处理数据的回调
    private val callback: ((ob: SdkValueObject<T>) -> Unit)? = null) {

    var oldValue: T? = null
    var newValue: T? = null

    var value: T by Delegates.observable(default){_, oldValue, newValue ->
        Logger.info("sdk init data change->[Target:$tag][oldValue:$oldValue][newValue:$newValue]")
        this.oldValue = oldValue
        this.newValue = newValue
        if (needSave) callback?.let { it(this) }
    }

    /**
     * 注册监听
     */
    fun injectListener(): Boolean{
        return watchable
    }

    /**
     * 丢弃监听
     */
    fun dropListener(){

    }
}