package com.superbutton.app.data

import android.content.Context
import android.content.SharedPreferences
import com.superbutton.app.model.ButtonAction

class PreferenceManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    var isServiceEnabled: Boolean
        get() = prefs.getBoolean(KEY_SERVICE_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_SERVICE_ENABLED, value).apply()

    var activeOpacity: Float
        get() = prefs.getFloat(KEY_ACTIVE_OPACITY, 0.70f)
        set(value) = prefs.edit().putFloat(KEY_ACTIVE_OPACITY, value).apply()

    var idleOpacity: Float
        get() = prefs.getFloat(KEY_IDLE_OPACITY, 0.20f)
        set(value) = prefs.edit().putFloat(KEY_IDLE_OPACITY, value).apply()

    var idleTimeoutSeconds: Int
        get() = prefs.getInt(KEY_IDLE_TIMEOUT, 3)
        set(value) = prefs.edit().putInt(KEY_IDLE_TIMEOUT, value).apply()

    var buttonSizeDp: Int
        get() = prefs.getInt(KEY_BUTTON_SIZE, 56)
        set(value) = prefs.edit().putInt(KEY_BUTTON_SIZE, value).apply()

    var isSnapToEdge: Boolean
        get() = prefs.getBoolean(KEY_SNAP_TO_EDGE, true)
        set(value) = prefs.edit().putBoolean(KEY_SNAP_TO_EDGE, value).apply()

    var isHapticEnabled: Boolean
        get() = prefs.getBoolean(KEY_HAPTIC_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_HAPTIC_ENABLED, value).apply()

    var lastPositionX: Int
        get() = prefs.getInt(KEY_LAST_X, -1)
        set(value) = prefs.edit().putInt(KEY_LAST_X, value).apply()

    var lastPositionY: Int
        get() = prefs.getInt(KEY_LAST_Y, -1)
        set(value) = prefs.edit().putInt(KEY_LAST_Y, value).apply()

    var singleTapAction: ButtonAction
        get() = ButtonAction.fromId(prefs.getString(KEY_SINGLE_TAP_ACTION, ButtonAction.LOCK_SCREEN.id))
        set(value) = prefs.edit().putString(KEY_SINGLE_TAP_ACTION, value.id).apply()

    var doubleTapAction: ButtonAction
        get() = ButtonAction.fromId(prefs.getString(KEY_DOUBLE_TAP_ACTION, ButtonAction.POWER_MENU.id))
        set(value) = prefs.edit().putString(KEY_DOUBLE_TAP_ACTION, value.id).apply()

    var longPressAction: ButtonAction
        get() = ButtonAction.fromId(prefs.getString(KEY_LONG_PRESS_ACTION, ButtonAction.SCREENSHOT.id))
        set(value) = prefs.edit().putString(KEY_LONG_PRESS_ACTION, value.id).apply()

    companion object {
        private const val PREF_NAME = "super_button_preferences"
        private const val KEY_SERVICE_ENABLED = "key_service_enabled"
        private const val KEY_ACTIVE_OPACITY = "key_active_opacity"
        private const val KEY_IDLE_OPACITY = "key_idle_opacity"
        private const val KEY_IDLE_TIMEOUT = "key_idle_timeout"
        private const val KEY_BUTTON_SIZE = "key_button_size"
        private const val KEY_SNAP_TO_EDGE = "key_snap_to_edge"
        private const val KEY_HAPTIC_ENABLED = "key_haptic_enabled"
        private const val KEY_LAST_X = "key_last_x"
        private const val KEY_LAST_Y = "key_last_y"
        private const val KEY_SINGLE_TAP_ACTION = "key_single_tap_action"
        private const val KEY_DOUBLE_TAP_ACTION = "key_double_tap_action"
        private const val KEY_LONG_PRESS_ACTION = "key_long_press_action"

        const val ACTION_PREFERENCE_CHANGED = "com.superbutton.app.ACTION_PREFERENCE_CHANGED"
    }
}
