package com.carota.lib.status.node

import com.carota.lib.common.uitls.LiveDataUtil
import com.carota.lib.common.uitls.Logger
import java.security.cert.Extension

class NodeValueObject<T>(
    //todo: 默认值
    default: T,
    //todo: TAG标签
    internal val tag: String,
    //todo: 处理数据的回调
    private val bindCallback: ((String, NodeValueObject<T>) -> Unit)? = null,
    //todo: unbind
    private val unBindCallback: ((className: String, tag: String) -> Unit)? = null,
    //todo: get ocean value
    private val oceanValueCallback: ((ob: NodeValueObject<T>) -> T)? = null,
    //todo: if allow to send observer to ocean
    private val observeAllow: Boolean = false) {

    var value: T = default

    var propertyMap: MutableMap<String,ObserverProperty> = mutableMapOf()

    //todo: storage observer of should be injected
    internal var liveData: LiveDataUtil<T> = LiveDataUtil()

    /**
     * @property className String: observer class name
     * @property isContinue Boolean: hold observer or not
     * @constructor
     */
    inner class ObserverProperty(private val className: String,
                                 val liveData:LiveDataUtil<T>,
                                 var isContinue: Boolean = false)

    /**
     * when ocean data changed, call back
     */
    internal fun observerCallBack(className: String){
        Logger.needDel("observerCallBack tag: $tag")
        if (propertyMap.containsKey(className)){
            Logger.info("$className observer job done, drop it")
            propertyMap.remove(className)
        }
    }

    /**
     * get data from ocean
     * @return T
     */
    fun oceanValue(): T{
        return oceanValueCallback?.let { it(this) } ?: value
    }

    fun bindObserver(className: String, liveData: LiveDataUtil<T>): NodeValueObject<T>?{
        if (!observeAllow)
            throw IllegalArgumentException("$className is not allowed to listen $tag")

        if (className.isEmpty()){
            Logger.error("NodeValueObject received empty class name")
            return null
        }

        //todo: not allowed repeat bind
        if (propertyMap.containsKey(className)){
            Logger.error("NodeValueObject received a class has already been bound")
            return null
        }

        Logger.info("Class $className bind $tag observer")
        propertyMap[className] = ObserverProperty(className, liveData)

        bindCallback?.let { it(className, this) }
        return this
    }

    fun unBindObserver(className: String){
        if (propertyMap.containsKey(className)){
            Logger.error("NodeValueObject unBindObserver received a class name not exit")
            return
        }
        unBindCallback?.let { it(className, tag) }
    }

    fun setIsContinue(className: String, isContinue: Boolean = false){
        propertyMap[className]?.isContinue = isContinue
    }

}