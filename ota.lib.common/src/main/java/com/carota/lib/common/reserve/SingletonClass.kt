package com.carota.lib.common.reserve

class SingletonClass {
}

/**
 * 饿汉模式
 */
object ehanModel{

}

/**
 * 懒汉模式
 * 非线程安全的，多线程中可能创建多个实例
 */
class LanhanModeUnSafe(){
    companion object{
        val instance by lazy(mode = LazyThreadSafetyMode.NONE) {
            LanhanModeUnSafe()
        }
    }
}

/**
 * 懒汉模式
 * 线程安全的
 * 缺点：每次使用都要检查同步，增加开销
 **/
class LanhanModelSafety(){
    private var mInstance: LanhanModelSafety? = null

    @Synchronized
    fun getInstance(): LanhanModelSafety {
        if (mInstance == null) {
            mInstance = LanhanModelSafety()
        }
        return mInstance!!
    }
}

/**
 * 双重校验模式
 */
class DoubleCheckModel(){
    companion object {
        val mInstance by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED){
            DoubleCheckModel()
        }
    }
}

/**
 * 静态内部实现
 * 推荐使用
 */
class StaticSingleton{
    companion object{
        val instance = SingletonHolder.holder
    }

    private object SingletonHolder{
        val holder = StaticSingleton()
    }
}