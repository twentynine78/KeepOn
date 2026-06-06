package fr.twentynine.keepon.domain.gateway

/**
 * Provides the installed app's version code.
 */
interface AppVersionProvider {
    fun getCurrentVersionCode(): Long
}
