package com.rvt.storagedemo.knox

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast




class KnoxAdminReceiver : DeviceAdminReceiver() {

    fun showToast(context: Context?, msg: CharSequence?) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }


    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        showToast(context, "Device admin enabled");
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        showToast(context, "Device admin disabled");
    }
}