package com.carota.lib.common.reserve

class GenericClass {

    //todo: 泛型用于扩展函数
    private fun <T> T.using(data: T){
        println(this)
        //todo: 这样写有风险，T类型不一定有toString方法
        println(data.toString())
    }

    //todo:
    private fun testUsing(){
        "using".using("data")
    }
}

class GenericAClass<T>(data: T) {
    //todo: reified关键字保存泛型T
    inline fun <reified T>test(data: T){
        T::class.java
    }
}

//todo: ************************************************************************
/**
 * out关键字: 协变
 * 特性：out只能使用在接口或类(抽象类)当中，不能在方法中使用
 * 作用；可以在构造中将泛型传递给父类，在父类中使用 as 将T类型的父类转化为子类进行使用
 */
class GenericOutClass(){
    private val dog: AnimalManager<Dog> = object : AnimalManager<Dog>{
        override fun getAnimal(): Dog {
            return Dog()
        }
    }

    fun useHandAnimal(){
        //todo: 如果AnimalManager的泛型不具有out协变功能，则不能直接传入dog参数，handAnimal希望一个的是一个Animal类型，而不是子类
        //todo: AnimalManager加入协变out后，允许这样操作
        handAnimal(dog)
    }

}

fun handAnimal(animalManager: AnimalManager<Animal>){

}

//todo: 使用关键字out, 此时的泛型T只能用作返回值，不能用作参数
interface AnimalManager<out T>{
    fun getAnimal(): T

    //todo: 这行代码不能使用T做为参数
//    fun setAnimal(animal: T)
}

abstract class Animal(){

}

class Dog(): Animal(){
    val name = "dog"
}


//todo: ************************************************************************

//todo: 逆协变in

interface IinClass<in T>{
    fun setT(value: T)
    //todo: 逆斜边不能用作返回值
//    fun getT():T
}

//todo: 泛型的通配符
fun tongPei(){
    val list: List<*> = listOf("121")
    val list1: List<*> = listOf(1)
    val list2: List<*> = listOf(0.1)
}

//todo: 泛型中的where使用
interface A
interface B
interface C

class useClass(){
    val na = MyClass<Impl1, Impl2>()
}
//todo: 一个接口实现类
class Impl1: A, C
class Impl2: B
//todo: 泛型T是接口A, C的实现, 泛型R是B的实现
class MyClass<T, R> where T : A, R: B, T: C {

}
