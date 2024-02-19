package com.carota.lib.status.pump

import com.carota.lib.common.uitls.LiveDataUtil
import com.carota.lib.common.uitls.Logger

class PumpValueObject<T>(
    //todo: 默认值
    default: T,
    //todo: 处理数据的回调
    private val bindCallback: ((String, PumpValueObject<T>) -> Unit)? = null,
    //todo: unbind
    private val unBindCallback: ((String, PumpValueObject<T>) -> Unit)? = null,
    //todo: get ocean value
    private val oceanValueCallback: (() -> T)? = null,
    //todo: if allow to send observer to ocean
    private val observeAllow: Boolean = false) {

    var value: T = default

    internal var property: ObserverProperty? = null

    /**
     * @property className String: observer class name
     * @property isContinue Boolean: hold observer or not
     * @constructor
     */
    inner class ObserverProperty(val className: String,
                                 val liveData:LiveDataUtil<T>,
                                 var isContinue: Boolean = false)

    /**
     * when ocean data changed, call back
     */
    internal fun changeBack(className: String){
        Logger.info("${property?.className} value change call back")
        property?.run {
            //todo: make ocean drop livedata after value changed
            if (!this.isContinue){ unBindObserver(this.className) }
        }
    }

    /**
     * when ocean drop observer, call back
     */
    internal fun dropBack(){
        Logger.info("${property?.className} drop observer call back")
        property = null
    }

    /**
     * get data from ocean with callback
     * @return T
     */
    fun oceanValue(): T{
        return oceanValueCallback?.let { it() } ?: value
    }

    /**
     * bind livedata to ocean, bind witch one decided by "bindCallback"
     * @param className String
     * @param liveData LiveDataUtil<T>
     * @return PumpValueObject<T>?
     */
    fun bindObserver(className: String, liveData: LiveDataUtil<T>): PumpValueObject<T>?{
        if (!observeAllow)
            throw IllegalArgumentException("$className is not allowed to listen, check the params of this object, and learn how to use it")

        if (className.isEmpty()){
            Logger.error("NodeValueObject received empty class name")
            return null
        }

        property?.run {
            if (this.className == className) {
                Logger.warn("this value had bind by class $className, no necessary bind again")
            }else {
                //todo: this value bind by a new class, remove old observer and bind new one
                unBindObserver(this.className)
                property = ObserverProperty(className, liveData)
            }
        } ?: run {
            Logger.drop("property is null, init it")
            Logger.drop("property className: $className")
            property = ObserverProperty(className, liveData)
            Logger.drop("property className1: ${property!!.className}")
        }

        bindCallback?.let { it(className, this) }
        return this
    }

    /**
     * make ocean drop the listen livedata, "unBindCallback" decide witch ocean number drop it
     * @param className String
     */
    fun unBindObserver(className: String){
        Logger.drop("unBindObserver className: ${property?.className}")
        Logger.drop("className: ${className}")
        if (property?.className != className){
            Logger.error("NodeValueObject unBindObserver received a class name not exit")
            return
        }
        unBindCallback?.let { it(className, this) }
    }

    /**
     * if we should keep listen after value changed
     * @param isContinue Boolean
     */
    fun setIsContinue(isContinue: Boolean = false){
        property?.isContinue = isContinue
    }

}