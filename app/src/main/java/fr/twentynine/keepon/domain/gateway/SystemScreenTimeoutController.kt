package fr.twentynine.keepon.domain.gateway

import fr.twentynine.keepon.domain.model.ScreenTimeout
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

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
     * system has applied the value or [adoptionWait] elapses — a caller running
     * under a tighter external budget (e.g. a broadcast receiver's 5-second cap)
     * passes a smaller wait so its code after the apply still gets to run.
     *
     * @return true if the system actually adopted the value, false if the write was
     * ignored by the device (some OEM ROMs accept the call but keep the old value).
     */
    suspend fun applyDesiredScreenTimeout(
        timeout: ScreenTimeout,
        adoptionWait: Duration = DEFAULT_ADOPTION_WAIT,
    ): Boolean

    /**
     * Returns and consumes the pending app-initiated desired timeout that matches
     * [currentTimeout] (the anti-collision signal), or null when the change did not
     * originate from the app (i.e. the user changed it from the system settings).
     */
    fun consumeDesiredScreenTimeout(currentTimeout: ScreenTimeout): ScreenTimeout?

    companion object {
        /**
         * Default ceiling for the system-adoption wait. Successful writes converge in
         * ~100 ms, so the ceiling is only reached on slow or non-applying
         * (OEM-restricted) devices.
         */
        val DEFAULT_ADOPTION_WAIT: Duration = 5.seconds
    }
}
