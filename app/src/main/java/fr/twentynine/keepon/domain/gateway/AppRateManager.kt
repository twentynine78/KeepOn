package fr.twentynine.keepon.domain.gateway

/**
 * Domain port for the in-app rating prompt: opening the store listing, checking
 * availability and reading the first-install time.
 *
 * Note: [needShowRateTip] still carries the "show the rate tip" rule; it is slated
 * to move into a dedicated use case when the ViewModels are wired (Phase 5).
 */
interface AppRateManager {
    fun openPlayStore()
    fun canRateApp(): Boolean
    fun getFirstInstallTime(): Long
    fun needShowRateTip(
        currentCount: Long,
        firstInstallTime: Long,
        canRateApp: Boolean,
    ): Boolean
}
