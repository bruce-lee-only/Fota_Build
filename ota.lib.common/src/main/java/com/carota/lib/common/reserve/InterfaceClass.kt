package com.carota.lib.common.reserve

interface A1Class{
    fun using(){

    }
}
interface B1Class{
    fun using(){

    }
}
interface C1Class{
    fun using(){

    }
}
class InterfaceClass: A1Class, B1Class, C1Class {
    //todo: 分别调用不同父类的实现
    override fun using() {
        super<A1Class>.using()
        super<B1Class>.using()
        super<C1Class>.using()
    }
}