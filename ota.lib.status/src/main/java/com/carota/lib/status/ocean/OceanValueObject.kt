package com.carota.lib.status.ocean

import com.carota.lib.common.uitls.LaunchUtil
import com.carota.lib.common.uitls.LiveDataUtil
import com.carota.lib.common.uitls.Logger
import kotlin.properties.Delegates

class OceanValueObject<T>(
    //todo: default value
    default:T,
    //todo: if we should post value first, then store to db
    private val isPostEarly: Boolean = false,
    //todo: save value to database
    private val save2Db: (T) -> Unit,
    //todo: callback for get memory value
    private val getValue:(OceanValueObject<T>) -> T) {

    companion object{
        const val DEFAULT_STRING        = ""

        const val DEFAULT_BOOLEAN       = false

        const val DEFAULT_INT           = -1

        const val DEFAULT_LONG          = -1L
    }

    //todo: use value data from memory, judge by yourself witch function you should use
    var value: T by Delegates.observable(default){_, _, newValue ->
        //todo: post value to all observer
        liveDataMap.takeIf { it.isNotEmpty() } ?.run {
            this.forEach { (className, liveData) ->
                Logger.info("Ocean object post value to $className value:$newValue")
                callBackMap[className]?.let { it(className) }

                //todo: post value to observer, observer maybe should not use the post value, but rather, use getValue or valueDb to get value
                isPostEarly.takeIf { it } ?.run {
                    liveData.postValue(newValue)
                    LaunchUtil.instance.launch { save2Db(newValue) }
                }?: run {
                    save2Db(newValue)
                    liveData.postValue(newValue)
                }
            }
        } ?: save2Db(newValue)
    }

    private var liveDataMap: MutableMap<String, LiveDataUtil<T>> = mutableMapOf()

    private var callBackMap: MutableMap<String, ((String) ->Unit)> = mutableMapOf()

    /**
     * use this function to get value from db or memory,
     * @return T: return value decided by callback
     */
    fun dbValue(): T {
        return getValue(this)
    }

    /**
     * insert observer to map
     */
    fun injectObserver(key: String, observer: LiveDataUtil<T>, callBack:(className: String) -> Unit){
        Logger.info("Ocean received observer key:$key")
        callBackMap[key] = callBack
        liveDataMap[key] = observer
    }

    /**
     * remove observer from map
     */
    fun dropObserver(key: String, block:() ->Unit){
        liveDataMap.remove(key)
        block()
    }

}