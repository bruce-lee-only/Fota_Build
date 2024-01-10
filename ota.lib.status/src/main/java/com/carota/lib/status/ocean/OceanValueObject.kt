package com.carota.lib.status.ocean

import com.carota.lib.common.uitls.LiveDataUtil
import com.carota.lib.common.uitls.Logger
import kotlin.properties.Delegates

class OceanValueObject<T>(
    //todo: default value
    default:T,
    //todo: callback for get memory value
    private val getValue:() -> T) {

    //todo: use value data from memory, judge by yourself witch function you should use
    var value: T by Delegates.observable(default){_, _, newValue ->
        //todo: post value to all observer
        liveDataMap.forEach { (className, liveData) ->
            Logger.info("Ocean object post value to $className value:$newValue")
            //todo: post value to observer, observer maybe should not use the post value, but rather, use getValue or valueDb to get get value
            callBackMap[className]?.let { it(className) }
            liveData.postValue(newValue)
        }
    }

    private var liveDataMap: MutableMap<String, LiveDataUtil<T>> = mutableMapOf()

    private var callBackMap: MutableMap<String, ((String) ->Unit)> = mutableMapOf()

    //todo: use this function to get value from db...
    fun value(): T {
        return getValue()
    }

    /**
     * insert observer to map
     */
    fun injectObserver(key: String, observer: LiveDataUtil<T>, callBack:(className: String) -> Unit){
        Logger.needDel("ocean received observer key:$key")
        callBackMap[key] = callBack
        liveDataMap[key] = observer
    }

    /**
     * remove observer from map
     */
    fun dropObserver(key: String){
        liveDataMap.remove(key)
    }
}