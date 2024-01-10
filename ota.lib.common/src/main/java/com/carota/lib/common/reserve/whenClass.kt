package com.carota.lib.common.reserve

class whenClass {
    val a: Any = 1
    fun useWhen(){
        when(a){
            //todo: 条件可以是区间
            in 1 .. 10 ->{}
            //todo: 可以是判断
            is Int  ->{}
        }
    }
}