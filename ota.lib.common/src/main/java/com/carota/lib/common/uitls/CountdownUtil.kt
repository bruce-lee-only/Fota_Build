package com.carota.lib.common.uitls

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class CountdownUtil {
    companion object{
        /**
         * run a callback after countdown
         * @param second unit: second
         */
        fun countdown(second: Int, block: () -> Unit){
            LaunchUtil.instance.launch {
                delay(second * 1000L)
                block()
            }
        }

        /**
         * call back range from big to small
         * if second is 0, run callback immediately
         * @param second Int
         * @param block Function1<[@kotlin.ParameterName] Int, Unit>
         */
        fun reverseOrder(second: Int, block: (time: Int) -> Unit){
            if (second == 0) block(second)
            LaunchUtil.instance.launch {
                for (i in second ..  1){
                    delay(1_000)
                    block(i)
                }
            }
        }

        /**
         * call back range from small to big
         * if second is 0, run callback immediately
         * @param second Int
         * @param block Function1<[@kotlin.ParameterName] Int, Unit>
         */
        fun positiveOrder(second: Int, isMain: Boolean = false, block: (time: Int) -> Unit){
            if (second == 0) block(second)
            LaunchUtil.instance.launch {
                for (i in 1 ..  second){
                    delay(1_000)
                    if (isMain) { withContext(Dispatchers.Main){ block(i) } } else block(i)
                }
            }
        }

        /**
         * attention: we should use job of return to finish this launch, otherwise, it will run always
         * @param step Int
         * @param isMain Boolean
         * @param end Function0<Unit>
         * @return Job
         */
        fun always(step: Int, isMain: Boolean = false,end: () -> Unit) = LaunchUtil.instance.launch{
            while (true){
                delay(step * 1000L)
                if (isMain) { withContext(Dispatchers.Main){ end() } } else end()
            }
        }
    }
}