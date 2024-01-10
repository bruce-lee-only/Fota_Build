package com.carota.lib.common.reserve

class FunctionClass {
    fun useFunction(){
        println(sum(1, 2))

        //todo: sum3只有Int对象才能调用
        println(2.sum3(3))
        println(2 sum3 5)

        //todo: 使用多参数
        sum4("1", "2", "3")
        //todo: 使用多参数,用*进行展开
        sum4(*arrayOf("1", "2", "3"))
    }

    private fun sum(a:Int, b:Int): Int {
        return a + b
    }

    //todo: 如果方法中只有一个返回值可以这样写
    private fun sum1(a: Int) = a

    //todo: 方法的实现交给另一个方法
    private fun sum2(a: Int,b: Int) = sum(a, b)

    //todo: 限定只有某类对象才可以调用的方法
    private infix fun Int.sum3(a: Int): Int { return a }

    //todo: 多参数与展开
    private fun sum4(vararg list: String){

    }

    //todo: 如果调用方法的时候参数顺序乱掉了，需要使用指向性的方式进行传参调用
}

//todo: 类的构造函数
class BClass(a: Int){
    constructor(): this(1)

    constructor(a: Int, b: String): this(a)

    constructor(c: String): this(1, c)
}