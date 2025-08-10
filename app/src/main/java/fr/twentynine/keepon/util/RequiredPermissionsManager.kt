package fr.twentynine.keepon.util

import android.content.Context

class RequiredPermissionsManager {
    companion object {
        fun isPermissionsGranted(context: Context): Boolean {
            return BatteryOptimizationManager.isBatteryNotOptimized(context) &&
                SystemSettingPermissionManager.canWriteSystemSettings(context)
        }
    }
}
