package fr.twentynine.keepon.domain.gateway

/**
 * Domain port controlling the foreground "screen off" service lifecycle.
 * The concrete implementation owns the running-state and intent action details.
 */
interface ScreenOffReceiverServiceManager {
    suspend fun startService()
    suspend fun stopService()
    suspend fun restartService()
}
