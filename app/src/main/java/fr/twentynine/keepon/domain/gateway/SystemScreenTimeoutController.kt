package fr.twentynine.keepon.domain.gateway

import fr.twentynine.keepon.domain.model.ScreenTimeout

/**
 * Domain port to read/write the system screen-off timeout (Settings.System).
 * Implemented by an outer-layer controller and injected via DI.
 */
interface SystemScreenTimeoutController {
    fun getSystemScreenTimeout(): ScreenTimeout
    fun setSystemScreenTimeout(timeout: ScreenTimeout)
}
