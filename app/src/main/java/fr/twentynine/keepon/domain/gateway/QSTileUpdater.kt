package fr.twentynine.keepon.domain.gateway

/**
 * Domain port to request a refresh of the Quick Settings tile.
 */
interface QSTileUpdater {
    fun requestUpdate()
}
