package fr.twentynine.keepon.domain.gateway

/**
 * Domain port to prompt the user to add the Quick Settings tile (Android 13+).
 */
interface AddTileServiceManager {
    fun requestAddTileService(successCallback: () -> Unit, errorCallback: (Int) -> Unit = {})
}
