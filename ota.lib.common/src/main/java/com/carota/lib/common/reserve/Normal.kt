package com.carota.lib.common.reserve

import android.widget.TextView
import kotlin.math.roundToInt

/**
 * normal use in kotlin
 */
class Normal {

    //todo: a function with a equals sign
    private fun odd(x: Int): Boolean = x % 2 == 1

    //todo: this function take a function as arguments, and return a function
    fun not(f: (Int) -> Boolean): (Int) -> Boolean {
        return {n -> !f.invoke(n)}
    }
    fun useNotFunction(){
        /**
         * add "odd" function to not, then get a new function
         * and run new function with argument “3”,
         */
        val ret: Boolean = not(::odd)(4)

        /**
         * lambda expression can be specified as argument
         */
        val notZero = not {n -> n != 4}

        /**
         * if lambda has only one argument, then its declaration can be omitted
         */
        val notPositive = not {it > 0}
    }
}

class InfixClass(){

    infix fun infixMemberFunction(y: Int): Int {
        return y
    }
}

//todo: infix notation using in outside of class, this mean only "InfixClass" can use this "infixMath" function
infix fun InfixClass.infixMath(str: String): String{
    println(str)
    return ""
}

class  UseInfixClass(){
    val a = InfixClass()
    //todo: you can use "infixMemberFunction" function like this with infix notation
    val ret = a infixMemberFunction 5
    val b = Normal()

    val ret1 =  a infixMath ""
}

data class DataClassExample (val x: Int, val y: Int, val z: Int){}
val fooData = DataClassExample(1, 2, 4)

// Data classes have a "copy" function.
val fooCopy = fooData.copy(y = 100)

fun useDataCopy(){
    println(fooCopy)// => DataClassExample(x=1, y=100, z=4)

    // Objects can be destructured into multiple variables.
    val (a, b, c) = fooCopy
    println("$a $b $c") // => 1 100 4

    // destructuring in "for" loop
    for ((d, e, f) in listOf(fooData)) {
        println("$d $e $f") // => 1 2 4
    }
}


// We can create a set using the "setOf" function.
//todo: set-> an unordered,non repeatable list
val fooSet = setOf("a", "b", "c")
fun useSetOf(){
    println(fooSet.contains("a")) // => true
    println(fooSet.contains("z")) // => false
}

/*
    Sequences represent lazily-evaluated collections.
    We can create a sequence using the "generateSequence" function.
    */
val fooSequence = generateSequence(1, { it + 1 })
val x = fooSequence.take(10).toList()
fun useGenerateSequence(){println(x)} // => [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]

// Kotlin provides higher-order functions for working with collections.
val z = (1..9).map {it * 3}
    .filter {it < 20}
    .groupBy {it % 2 == 0}
    .mapKeys {if (it.key) "even" else "odd"}
fun useZ(){println(z)} // => {odd=[3, 9, 15], even=[6, 12, 18]}

// Since each enum is an instance of the enum class, they can be initialized as:
enum class EnumExample(val value: Int) {
    A(value = 1),
    B(value = 2),
    C(value = 3)
}

//todo: if context transform activity fail, it will bi null,and stop to call finish function
fun asFunction(){
    //(context as? Activity)?.finish()
}

/**
 * 比如带参数的创建操作，官方通常会用 listOf()、mapOf() 等 xxxOf() 的命名，建议与官方统一，
 * 不建议用 createXXX() 或者 newXXX() 等命名
 */

//todo: we can use "is" to judge similar
fun useIsFunction(any: Any){
    if (any is Int) return
    if (any !is String) return
}

//todo: we use "===" to determine whether two references point to same object
fun useTheeEqule(){

}

//todo: 安全校验符可以进行链式调用
fun useSecurityCall(){
//    val i = 1?.2?.3? ?: 2
}


