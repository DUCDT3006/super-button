package com.superbutton.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import com.superbutton.app.data.PreferenceManager
import com.superbutton.app.services.FloatingButtonService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val prefManager = PreferenceManager(context)
            if (prefManager.isServiceEnabled && Settings.canDrawOverlays(context)) {
                val serviceIntent = Intent(context, FloatingButtonService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
            }
        }
    }
}
