package com.superbutton.app.model

import com.superbutton.app.R

enum class ButtonAction(val id: String, val titleResId: Int, val iconResId: Int) {
    LOCK_SCREEN("lock_screen", R.string.action_lock_screen, R.drawable.ic_lock),
    POWER_MENU("power_menu", R.string.action_power_menu, R.drawable.ic_power),
    SCREENSHOT("screenshot", R.string.action_screenshot, R.drawable.ic_screenshot),
    RADIAL_MENU("radial_menu", R.string.action_radial_menu, R.drawable.ic_power),
    VOLUME_PANEL("volume_panel", R.string.action_volume, R.drawable.ic_volume),
    NOTIFICATIONS("notifications", R.string.action_notifications, R.drawable.ic_notification),
    RECENTS("recents", R.string.action_recents, R.drawable.ic_recents),
    HOME("home", R.string.action_home, R.drawable.ic_home),
    BACK("back", R.string.action_back, R.drawable.ic_back);

    companion object {
        fun fromId(id: String?): ButtonAction {
            return entries.firstOrNull { it.id == id } ?: LOCK_SCREEN
        }
    }
}
