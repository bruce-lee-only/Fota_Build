package com.carota.lib.common.reserve

class DoubleColonClass {
    val doubleData = "1212121"
    private val temp = ::temp1
    private fun temp1(){

    }

    private fun test(lambda:()->Unit){

    }

    /**
     * 直接使用引用作为lambda表达式
     */
    fun usingTest(){
        test(temp)
    }

}

/**
 *
 */
class usingClass(){
    /**
     * 使用其他类的方法引用
     */
    var tmp = DoubleColonClass::usingTest

    /**
     * 使用其他类的变量引用
     */
    val data = DoubleColonClass::doubleData

    /**
     * 引用类似于反射，目前我们任务可以当场反射来使用
     *
     */

    /**
     * class：kotlin类中的元数据 相对于class.java内容丰富
     * class.java： 对应的java的类引用
     */
}