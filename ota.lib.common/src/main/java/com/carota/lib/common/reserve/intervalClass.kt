package com.carota.lib.common.reserve

class intervalClass {

    fun usrInterval(){
        1 .. 10

        @OptIn(ExperimentalStdlibApi::class)
        1 ..< 10

        1 until 10

        10 downTo 1

        val list = (1 .. 10 step 2).toList()

        (1 .. 10 step 2).forEach { println(it) }

        'a' .. 'z'
     }
}