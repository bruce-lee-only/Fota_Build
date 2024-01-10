package com.carota.lib.ue

abstract class NodeBase {
    val className: String = this::class.java.canonicalName ?: ""

    abstract fun run()
}