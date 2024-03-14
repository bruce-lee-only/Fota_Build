package com.carota.lib.executor.ui.uiHelper

@Retention(AnnotationRetention.RUNTIME)
annotation class Debounce(val interval: Long = 1000)

object DebounceProxy {
    inline fun <reified T : Any> bind(target: T): T {
        val delegatedProperties = mutableMapOf<String, ClickDebounce>()
        return java.lang.reflect.Proxy.newProxyInstance(
            target.javaClass.classLoader,
            arrayOf(T::class.java)
        ) { _, method, args ->
            val annotation = method.getAnnotation(Debounce::class.java)
            if (annotation != null) {
                val methodName = method.name
                val debounce = delegatedProperties.getOrPut(methodName) {
                    ClickDebounce(annotation.interval)
                }
                debounce.invoke(target, method, args ?: emptyArray())
            } else {
                method.invoke(target, *(args ?: emptyArray()))
            }
        } as T
    }
}

class ClickDebounce(private val interval: Long) {
    private var lastClickTime: Long = 0

    fun invoke(target: Any, method: java.lang.reflect.Method, args: Array<Any?>) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime >= interval) {
            lastClickTime = currentTime
            method.invoke(target, *args)
        }
    }
}