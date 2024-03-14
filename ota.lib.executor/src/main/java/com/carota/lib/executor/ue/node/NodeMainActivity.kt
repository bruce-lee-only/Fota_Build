package com.carota.lib.executor.ue.node

import android.content.Context
import android.content.Intent
import com.carota.lib.executor.ui.activity.mainActivity.MainActivity

class NodeMainActivity(private val context: Context): NodeBase() {
    override fun run() {
        super.run()
        val intent = Intent(context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}