package fr.twentynine.keepon.domain.gateway

import fr.twentynine.keepon.domain.model.ScreenTimeout

/**
 * Domain port to read/write the system screen-off timeout (Settings.System).
 * Implemented by an outer-layer controller and injected via DI.
 */
interface SystemScreenTimeoutController {
    fun getSystemScreenTimeout(): ScreenTimeout
    fun setSystemScreenTimeout(timeout: ScreenTimeout)

    /**
     * Applies [timeout] through the anti-collision desired-timeout queue so the
     * monitor worker recognizes the change as app-initiated. Suspends until the
     * system has applied the value (or a short timeout elapses).
     *
     * @return true if the system actually adopted the value, false if the write was
     * ignored by the device (some OEM ROMs accept the call but keep the old value).
     */
    suspend fun applyDesiredScreenTimeout(timeout: ScreenTimeout): Boolean

    /**
     * Returns and consumes the pending app-initiated desired timeout that matches
     * [currentTimeout] (the anti-collision signal), or null when the change did not
     * originate from the app (i.e. the user changed it from the system settings).
     */
    fun consumeDesiredScreenTimeout(currentTimeout: ScreenTimeout): ScreenTimeout?
}
