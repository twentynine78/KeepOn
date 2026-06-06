package fr.twentynine.keepon.domain.gateway

/**
 * Domain port to refresh the home-screen widget surface.
 */
interface WidgetUpdater {
    suspend fun requestUpdateWidget()
    suspend fun requestUpdateWidgetPreview(): Boolean
}
