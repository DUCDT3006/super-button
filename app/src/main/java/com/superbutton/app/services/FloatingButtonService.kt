package com.superbutton.app.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.superbutton.app.MainActivity
import com.superbutton.app.R
import com.superbutton.app.data.PreferenceManager
import com.superbutton.app.ui.FloatingViewManager

class FloatingButtonService : Service() {

    private var floatingViewManager: FloatingViewManager? = null

    private val preferenceReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == PreferenceManager.ACTION_PREFERENCE_CHANGED) {
                floatingViewManager?.reloadPreferences()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())

        floatingViewManager = FloatingViewManager(this)
        floatingViewManager?.show()

        val filter = IntentFilter(PreferenceManager.ACTION_PREFERENCE_CHANGED)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(preferenceReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(preferenceReceiver, filter)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_SERVICE) {
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null

        try {
            unregisterReceiver(preferenceReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        floatingViewManager?.hide()
        floatingViewManager = null
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_desc)
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_text))
            .setSmallIcon(R.drawable.ic_power)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    companion object {
        const val CHANNEL_ID = "super_button_service_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_STOP_SERVICE = "com.superbutton.app.ACTION_STOP_SERVICE"

        var instance: FloatingButtonService? = null
            private set

        val isRunning: Boolean
            get() = instance != null
    }
}
