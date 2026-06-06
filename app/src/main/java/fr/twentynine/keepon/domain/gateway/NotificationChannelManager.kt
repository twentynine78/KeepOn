package fr.twentynine.keepon.domain.gateway

/**
 * Domain port to clean up notification channels left over from older app versions.
 */
interface NotificationChannelManager {
    fun removeLegacyKeepOnServiceChannel()
}
