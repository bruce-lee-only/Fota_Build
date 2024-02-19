package com.carota.lib.common.reserve

import android.view.View
import android.widget.TextView
import kotlin.math.roundToInt

class SpreadFunction {
}

//todo: use spread function
fun SpreadFunction.isBoolean() = this.apply {}

//todo: we can use spread function to transform px to dp, such as that
fun Int.toDp():Int = this.let {
    val f: Double = this / 1.5
    f.roundToInt()
}

//todo: we can create a View spread function to realize hide it
fun View.visible() = this.apply { this.visibility = View.VISIBLE }