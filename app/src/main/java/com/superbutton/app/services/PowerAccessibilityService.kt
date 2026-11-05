package com.superbutton.app.services

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.superbutton.app.model.ButtonAction

class PowerAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        Log.d(TAG, "PowerAccessibilityService connected successfully")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Not used, but required by abstract class
    }

    override fun onInterrupt() {
        Log.w(TAG, "PowerAccessibilityService interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        if (instance == this) {
            instance = null
        }
        Log.d(TAG, "PowerAccessibilityService destroyed")
    }

    fun executeAction(action: ButtonAction): Boolean {
        Log.d(TAG, "Executing action: ${action.name}")
        return when (action) {
            ButtonAction.LOCK_SCREEN -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
                } else {
                    false
                }
            }
            ButtonAction.POWER_MENU -> {
                performGlobalAction(GLOBAL_ACTION_POWER_DIALOG)
            }
            ButtonAction.SCREENSHOT -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    performGlobalAction(GLOBAL_ACTION_TAKE_SCREENSHOT)
                } else {
                    false
                }
            }
            ButtonAction.VOLUME_PANEL -> {
                val audioManager = getSystemService(Context.AUDIO_SERVICE) as? AudioManager
                audioManager?.adjustVolume(AudioManager.ADJUST_SAME, AudioManager.FLAG_SHOW_UI)
                true
            }
            ButtonAction.NOTIFICATIONS -> {
                performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS)
            }
            ButtonAction.RECENTS -> {
                performGlobalAction(GLOBAL_ACTION_RECENTS)
            }
            ButtonAction.HOME -> {
                performGlobalAction(GLOBAL_ACTION_HOME)
            }
            ButtonAction.BACK -> {
                performGlobalAction(GLOBAL_ACTION_BACK)
            }
            ButtonAction.RADIAL_MENU -> {
                // Handled directly by FloatingViewManager
                true
            }
        }
    }

    companion object {
        private const val TAG = "PowerAccessibility"
        var instance: PowerAccessibilityService? = null
            private set

        val isServiceRunning: Boolean
            get() = instance != null
    }
}
