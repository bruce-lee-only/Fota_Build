package com.carota.lib.common.uitls

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import java.util.concurrent.atomic.AtomicBoolean

class LiveDataUtil<T>: MediatorLiveData<T>() {

    //todo: 消息通知锁
    private val messageLock: AtomicBoolean = AtomicBoolean(true)

    //todo: 监听控制器
    private var stopObserver : Boolean = false

    /**
     * 重写observe方法
     * @param owner LifecycleOwner
     * @param observer Observer<in T>
     */
    override fun observe(owner : LifecycleOwner, observer : Observer<in T>) {
        observe(owner, false, observer)
    }

    /**
     * 重构observe方法，增加粘性参数
     * used: witch can provide lifecycle such as activity, fragment
     * @param owner LifecycleOwner
     * @param stick Boolean: 是否保持粘性
     * @param observer Observer<in T>
     */
    fun observe(owner : LifecycleOwner, stick : Boolean = false, observer : Observer<in T>) {
        //新来的观察者锁住消息通知
        messageLock.set(true)
        super.observe(owner, WrapperObserver(stick, observer))
    }

    fun stopObserver(){
        stopObserver = true
    }

    fun startObserver(){
        stopObserver = false
    }

    open inner class WrapperObserver(
        private val stick: Boolean = false, //是否是粘性观察，是的话会收到订阅之前的消息
        private val observer: Observer<in T>,
    ) : Observer<T> {
        //todo: 是否是新加入的观察者
        private val isFirstObserver : AtomicBoolean = AtomicBoolean(true)

        override fun onChanged(t : T) {
            if(messageLock.get() && !stick) {
                //todo: 当前是锁定状态非粘性观察者，且是新的观察者，设置成不是新观察者。如果不是新观察者，则通知
                if(isFirstObserver.compareAndSet(false, false)) {
                    onWrapperChanged(t)
                }
            } else {
                //todo: 非锁定状态或粘性观察者，修改为不是新观察者，接收通知
                isFirstObserver.set(false)
                onWrapperChanged(t)
            }
        }

        private fun onWrapperChanged(t : T){
            takeIf { stopObserver } ?: observer.onChanged(t)
        }
    }
}