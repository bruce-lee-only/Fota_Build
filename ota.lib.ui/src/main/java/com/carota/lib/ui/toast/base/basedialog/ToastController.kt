package com.carota.lib.ui.toast.base.basedialog

import android.content.Context
import android.view.WindowManager
import android.content.ContextWrapper
import android.os.Looper
import android.view.LayoutInflater
import android.os.Handler
import android.os.Message
import android.view.View
import androidx.databinding.ViewDataBinding
import java.lang.RuntimeException

class ToastController internal constructor(base: Context?) : ContextWrapper(base) {
    private val mWindowManager: WindowManager = getSystemService(WINDOW_SERVICE) as WindowManager
    var isShow: Boolean
        private set
    private var mLayoutParams: WindowManager.LayoutParams? = null
    private lateinit var mDoc: View
    private val mHandler = ToastHandler()

    private var mShowListener: ((View) -> Unit)? = null
    private var mDismissListener: (() -> Unit)? = null
    fun show(): Boolean {
        if (!isShow) {
            isShow = true
            mHandler.sendEmptyMessage(MSG_ADD)
            return true
        }
        return false
    }

    fun dismiss(): Boolean {
        if (isShow) {
            isShow = false
            mHandler.sendEmptyMessage(MSG_REMOVE)
            return true
        }
        return false
    }

    fun setLayoutParams(params: WindowManager.LayoutParams?) {
        mLayoutParams = params
    }

    fun setDocView(v: View) {
        mDoc = v
    }

    fun setDismissListener(listener: (() -> Unit)?) {
        mDismissListener = listener
    }

    fun setShowListener(listener: ((View) -> Unit)?) {
        mShowListener = listener
    }

    inner class ToastHandler : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                MSG_ADD -> {
                    mWindowManager.addView(mDoc, mLayoutParams)
                    mShowListener?.let {
                        it(mDoc)
                    }
                }
                MSG_REMOVE -> {
                    mWindowManager.removeViewImmediate(mDoc)
                    mDismissListener?.let {
                        it()
                    }
                }
                else -> {}
            }
        }
    }

    class ToastParams internal constructor(context: Context?) {
        var mParams: WindowManager.LayoutParams? = null
        private val mContext: Context
        private var mView: View?
        private val mInflater: LayoutInflater
        private var mBinding: ViewDataBinding? = null
        private var mShowListener: ((View) -> Unit)? = null
        private var mDismissListener: (() -> Unit)? = null
        fun setLayoutParams(params: WindowManager.LayoutParams?) {
            if (params == null) {
                return
            }
            mParams = params
        }

        fun setView(layoutResId: Int) {
            val view = mInflater.inflate(layoutResId, null)
            setView(view)
        }

        fun setView(view: View?) {
            mView = view
        }

        fun apply(): ToastController {
            if (mView == null) throw RuntimeException("View is null")
            val toast = ToastController(mContext)
            toast.setDocView(mView!!)
            toast.setLayoutParams(mParams)
            toast.setDismissListener(mDismissListener)
            toast.setShowListener(mShowListener)
            return toast
        }

        fun setShowListener(listener: ((View) -> Unit)?) {
            mShowListener = listener
        }

        fun setDismissListener(listener: (() -> Unit)?) {
            mDismissListener = listener
        }

        fun setViewDataBinding(binding: ViewDataBinding?){
            mBinding = binding
        }

        init {
            requireNotNull(context) { "Context is null" }
            mContext = context.applicationContext
            mInflater = LayoutInflater.from(context.applicationContext)
            mView = null
        }
    }

    companion object {
        private const val MSG_ADD = 1
        private const val MSG_REMOVE = 2
    }

    init {
        isShow = false
    }
}