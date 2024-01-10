package com.carota.lib.status.river.sdk

sealed interface IValueChanged{
    fun <T>changeHandler( oldValue:T, newValue:T)
}