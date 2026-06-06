package fr.twentynine.keepon.domain.gateway

/**
 * Domain port for the in-app rating prompt: opening the store listing, checking
 * availability and reading the first-install time.
 */
interface AppRateManager {
    fun openPlayStore()
    fun canRateApp(): Boolean
    fun getFirstInstallTime(): Long
}
