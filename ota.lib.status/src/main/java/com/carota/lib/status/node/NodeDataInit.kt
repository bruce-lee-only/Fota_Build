package com.carota.lib.status.node

import com.carota.lib.common.uitls.LiveDataUtil
import com.carota.lib.common.uitls.Logger
import com.carota.lib.status.ocean.OceanSdkData

class NodeDataInit: NodeBase() {
    /**
     * SDK初始化是否结束
     */
    private val tagInitCareIsFinish = "initCareIsFinish"
    var initCareIsFinish: NodeValueObject<Boolean> = NodeValueObject(
        false, tagInitCareIsFinish,
        ::bindObserver, ::unBindObserver,
        ::getData, false)

    val isResume: Boolean       get() = OceanSdkData.INSTANCE.sdkIsResume.value

    val initResult: Boolean       get() = OceanSdkData.INSTANCE.sdk.value

   override fun <T> bindOcean(className: String, obj: NodeValueObject<T>) {
        when(obj.tag){
            tagInitCareIsFinish     -> {
                Logger.needDel("inject observer to ocean")
                obj.let {
                    OceanSdkData.INSTANCE.sdkIsInitFinish.injectObserver(
                        className,
                        it.propertyMap[className]!!.liveData as LiveDataUtil<Boolean>,
                        it::observerCallBack)
                }
            }
            else    -> { Logger.error("NodeInit bindOcean error: hava no ${obj.tag} tag")}
        }
    }

    override fun unBindOcean(className: String, tag: String) {
        when(tag){
            tagInitCareIsFinish     -> {
                Logger.needDel("free observer from ocean")
                OceanSdkData.INSTANCE.sdkIsInitFinish.dropObserver(className)
            }
            else    -> { Logger.error("NodeInit unbind Ocean error: hava no $tag tag")}
        }
    }

    /**
     * @param obj NodeValueObject<T>
     * @return T: attention, we must know the type of "T", turn to type T can be anything
     */
    override fun <T> getOceanData(obj: NodeValueObject<T>): T {
        return when(obj.tag){
            tagInitCareIsFinish     -> {
                OceanSdkData.INSTANCE.sdkIsInitFinish.value as T
            }
            else -> {
                Logger.error("NodeInit get ocean data error: have no ${obj.tag} Tag")
                //todo: tag not exist, return itself value
                obj.value
            }
        }
    }

}