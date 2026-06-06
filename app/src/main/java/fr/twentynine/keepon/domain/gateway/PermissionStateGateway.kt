package fr.twentynine.keepon.domain.gateway

import kotlinx.coroutines.flow.Flow

/**
 * Domain port exposing the current grant state of the permissions the app needs,
 * both as observable flows and as one-shot checks. The grant state is refreshed by
 * the UI layer (on resume / after a permission request).
 */
interface PermissionStateGateway {
    val canWriteSystemSetting: Flow<Boolean>
    val batteryIsNotOptimized: Flow<Boolean>
    val canPostNotification: Flow<Boolean>

    fun canWriteSystemSettings(): Boolean
    fun isBatteryNotOptimized(): Boolean
    fun areRequiredPermissionsGranted(): Boolean

    fun refreshWriteSystemSettings()
    fun refreshBatteryOptimization()
    fun refreshPostNotification()
    fun setPostNotificationGranted(granted: Boolean)
}
