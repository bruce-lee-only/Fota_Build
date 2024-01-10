package com.carota.lib.common.reserve

class ForClass {
    val list:List<Int> = (1 .. 10).toList()
    fun useFor(){
        //todo: 迭代器返回对象
        for ((index: Int, value: Int) in list.withIndex()) {
            println("$value")
        }
        //todo: 迭代器返回对象
        for (i:IndexedValue<Int> in list.withIndex()) {
            println("${i.value}")
        }

        //todo: forEach的下标跟value
        list.forEachIndexed() {index, value ->
            println("$value")
            println("$index")
        }

        //todo: forEach中的返回操作, 使用run block块儿让forEach可以返回
        kotlin.run interrupt@{
            (1 .. 10).forEach {
                if (it == 4) return@interrupt
            }
        }
    }
}