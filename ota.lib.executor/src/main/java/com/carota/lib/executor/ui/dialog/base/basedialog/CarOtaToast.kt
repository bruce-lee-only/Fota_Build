package com.carota.lib.executor.ui.dialog.base.basedialog

import android.content.Context
import android.view.WindowManager
import android.view.View
import androidx.databinding.ViewDataBinding

/**
 * Carota Toast
 */
class CarOtaToast private constructor(private val mCtrl: ToastController?, val level: Int) {
    fun show() = mCtrl!!.show()

    fun dismiss() = mCtrl!!.dismiss()

    val isShow: Boolean
        get() = mCtrl!!.isShow

    class Builder(context: Context) {
        private val tParams: ToastController.ToastParams
        var level: Int
            private set

        /**
         * 设置Toast背景参数
         */
        fun setParams(params: WindowManager.LayoutParams?): Builder {
            tParams.setLayoutParams(params)
            return this
        }

        fun setView(layoutResId: Int, view: View?): Builder {
            return if (view != null){
                setView(view)
            }else{
                require(layoutResId == 0) { "layoutResId not find" }
                tParams.setView(layoutResId)
                this
            }
        }

        private fun setView(view: View?): Builder {
            requireNotNull(view) { "View is null" }
            tParams.setView(view)
            return this
        }

        fun setShowListener(listener: ((View) -> Unit)?): Builder {
            tParams.setShowListener(listener)
            return this
        }

        fun setBinding(binding: ViewDataBinding?): Builder {
            tParams.setViewDataBinding(binding)
            return this
        }

        fun setDismissListener(listener: (() -> Unit)?): Builder {
            tParams.setDismissListener(listener)
            return this
        }

        fun paramsIsNull(): Boolean {
            return tParams.mParams == null
        }

        fun setLevel(priority: Int): Builder {
            level = priority
            return this
        }

        fun build(): CarOtaToast {
            val apply = tParams.apply()
            return CarOtaToast(apply, level)
        }

        init {
            tParams = ToastController.ToastParams(context)
            level = Int.MIN_VALUE
        }
    }
}