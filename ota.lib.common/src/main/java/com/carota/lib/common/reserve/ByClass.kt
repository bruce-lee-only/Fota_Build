package com.carota.lib.common.reserve

import kotlin.properties.Delegates
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class ByClass {
}

interface HomeDao{
    fun getString(): String
}

class HomeDaoImpl: HomeDao{
    override fun getString(): String {
        return "返回我"
    }
}

/**
 * 类的委托
 * 这里只有对接口可以进行委托实现，普通类、抽象类不能进行这种委托
 */
class HomeService(homeDaoImpl: HomeDaoImpl): HomeDao by homeDaoImpl{

}


/**
 * 属性的委托
 */
class ByProperty{
    var property: String by PropertyBuilder()
}

class PropertyBuilder : ReadWriteProperty<ByProperty, String> {
    override fun getValue(thisRef: ByProperty, property: KProperty<*>): String {
        return "getValue"
    }

    override fun setValue(thisRef: ByProperty, property: KProperty<*>, value: String) {

    }
}

/**
 * by lazy
 * 延时加载
 */
val byLazy by lazy {
    System.out.println("初始化byLazy")
    ByLazy()
}
class ByLazy(){

}

/**
 * 监听委托
 * 数据改变了进行监听
 */
class ByObserver(){
    var observer: String by Delegates.observable("oberver"){_, old, new ->

    }
}