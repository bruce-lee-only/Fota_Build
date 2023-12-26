package com.carota.lib.ui.toast.base.basedialog

import android.annotation.SuppressLint
import android.graphics.PixelFormat
import android.os.Build
import android.view.View
import android.view.WindowManager

@Suppress("DEPRECATION")
@SuppressLint("WrongConstant")
class ToastManager private constructor(params: WindowManager.LayoutParams? = null) {
    private var mParams: WindowManager.LayoutParams? = null
    private val mDefParams: WindowManager.LayoutParams

    private var mToast: CarOtaToast? = null

    fun show(builder: CarOtaToast.Builder?): Boolean {
        requireNotNull(builder) { "CarOtaToast.Builder is null" }
        if (isShow) {
            if (builder.level <= mToast!!.level) {
                return false
            } else {
                dismiss()
            }
        }
        if (builder.paramsIsNull()) {
            if (mParams != null) {
                builder.setParams(mParams)
            } else {
                builder.setParams(mDefParams)
            }
        }
        mToast = builder.build()
        return mToast!!.show()
    }

    fun dismiss() {
        mToast?.dismiss()
        mToast = null
    }

    val isShow: Boolean
        get() = mToast != null && mToast!!.isShow

    companion object {
        private var mManager: ToastManager? = null
        fun get(): ToastManager {
            if (mManager == null) {
                synchronized(ToastManager::class.java) {
                    if (mManager == null) mManager = ToastManager()
                }
            }
            return mManager!!
        }
    }


    init {

        val systemUiVisibility =
            (View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.STATUS_BAR_VISIBLE
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

        mDefParams = WindowManager.LayoutParams()
        //默认填充父窗口
        mDefParams.height = WindowManager.LayoutParams.MATCH_PARENT
        mDefParams.width = WindowManager.LayoutParams.MATCH_PARENT
        //录入传入参数
        params?.let {
            mDefParams.height    = it.height
            mDefParams.width     = it.width
        }

        mDefParams.format            = PixelFormat.TRANSLUCENT
        mDefParams.windowAnimations  = android.R.style.Animation_Dialog
        mDefParams.systemUiVisibility =  systemUiVisibility
        /**
         * FLAG_TRANSLUCENT_NAVIGATION: 导航栏半透明效果
         * FLAG_LAYOUT_IN_SCREEN:窗口占据整个屏幕空间，包括状态栏、导航栏等
         */
        mDefParams.flags = (mDefParams.flags
                    or WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
                    or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mDefParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR
        } else {
            mDefParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR
        }
    }
}