package fr.twentynine.keepon.domain.gateway

/**
 * Domain port exposing the current grant state of the permissions the app needs.
 */
interface PermissionStateGateway {
    fun canWriteSystemSettings(): Boolean
    fun isBatteryNotOptimized(): Boolean
    fun areRequiredPermissionsGranted(): Boolean
}
