package com.ayan.vult.receiver

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast

class VultAdminReceiver : DeviceAdminReceiver() {
    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        Log.d("VultAdmin", "Device Admin Enabled")
        Toast.makeText(context, "Vult Device Admin Enabled", Toast.LENGTH_SHORT).show()
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        Log.d("VultAdmin", "Device Admin Disabled")
        Toast.makeText(context, "Vult Device Admin Disabled", Toast.LENGTH_SHORT).show()
    }

    override fun onDisableRequested(context: Context, intent: Intent): CharSequence? {
        Log.d("VultAdmin", "Device Admin Disable Requested")
        return "Disabling Vult Device Admin will reduce the security of your device."
    }
}
