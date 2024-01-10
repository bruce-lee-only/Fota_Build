package com.carota.lib.common.uitls

import kotlinx.coroutines.*

class LaunchUtil {

    @OptIn(ExperimentalCoroutinesApi::class)
    //todo: start immediately without suspend, warning: this launch will not suspended
    val atomic          = CoroutineStart.ATOMIC
    //todo: start by lazy, run block at first run time, we can use start() to run the launch
    val lazy            = CoroutineStart.LAZY
    //todo: start launch immediately, but not run block at first time, and run block at comfortable time
    val default         = CoroutineStart.DEFAULT
    //todo: start by unDispatched, run in the thread of current
    val unDispatched    = CoroutineStart.UNDISPATCHED

    companion object{
        val instance = SingletonHolder.holder
    }

    private object SingletonHolder{
        val holder = LaunchUtil()
    }

    /**
     * create a coroutineScope launch without coroutineScope
     * @param dispatcher CoroutineDispatcher
     * @param start CoroutineStart
     * @param block [@kotlin.ExtensionFunctionType] SuspendFunction1<CoroutineScope, Unit>
     * @return Job
     */
    fun launch(
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
        start: CoroutineStart = default,
        block : suspend CoroutineScope.() -> Unit) : Job{
        return launch(CoroutineScope(dispatcher), start, dispatcher, block)
    }

    /**
     * create a coroutineScope launch with coroutineScope
     * @param dispatcher CoroutineDispatcher
     * @param start CoroutineStart
     * @param block [@kotlin.ExtensionFunctionType] SuspendFunction1<CoroutineScope, Unit>
     * @return Job
     */
    fun launch(
        coroutineScope: CoroutineScope,
        start: CoroutineStart = default,
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
        block: suspend CoroutineScope.() -> Unit): Job{
        return coroutineScope.launch(dispatcher, start = start, block = block)
    }

    /**
     * create async to wait current launch complete without CoroutineScope
     * @param start CoroutineStart
     * @param dispatcher CoroutineDispatcher
     * @param block [@kotlin.ExtensionFunctionType] SuspendFunction1<CoroutineScope, R>
     * @return Deferred<R>
     */
    fun <R>async(
        start: CoroutineStart = default,
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
        block: suspend CoroutineScope.() -> R
    ): Deferred<R>{
        return async(CoroutineScope(dispatcher), start = start, dispatcher, block = block)
    }

    /**
     * create async to wait current launch complete with CoroutineScope
     * @param start CoroutineStart
     * @param dispatcher CoroutineDispatcher
     * @param block [@kotlin.ExtensionFunctionType] SuspendFunction1<CoroutineScope, R>
     * @return Deferred<R>
     */
    fun <R>async(
        coroutineScope: CoroutineScope,
        start: CoroutineStart = default,
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
        block: suspend CoroutineScope.() -> R
    ): Deferred<R>{
        return coroutineScope.async(dispatcher, start = start, block = block)
    }

    /**
     * run a blocking main thread launch
     * attention: we should use this carefully, this function will blocking main-thread
     * @param block [@kotlin.ExtensionFunctionType] SuspendFunction1<CoroutineScope, Job>
     * @return Job
     */
    fun <T>blockingLaunch(
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
        block: suspend CoroutineScope.() ->T): T{
        return runBlocking(dispatcher, block = block)
    }

    suspend fun coroutineScopeBlock(
        block: suspend CoroutineScope.() -> Unit){
        coroutineScope(block = block)
    }
}