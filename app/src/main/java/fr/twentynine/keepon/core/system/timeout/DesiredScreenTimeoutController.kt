package fr.twentynine.keepon.core.system.timeout

import fr.twentynine.keepon.domain.gateway.SystemScreenTimeoutController
import fr.twentynine.keepon.domain.model.ScreenTimeout
import fr.twentynine.keepon.core.util.removeUntil
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.LinkedBlockingQueue

/**
 * Anti-collision queue for app-initiated screen-timeout writes. It records each requested value as
 * "desired" before writing it and then waits for the system to actually reflect it, so the monitor
 * worker can recognize the change as app-initiated (via [getDesiredScreenTimeout]) rather than a
 * user edit in system settings. A mutex serializes overlapping requests, and writes that the system
 * never adopts (some OEM ROMs accept the call but keep the old value) are reported as not applied.
 */
object DesiredScreenTimeoutController {
    private val defaultDispatchers = Dispatchers.Default
    private val screenTimeoutProcessingLock = Mutex()

    private val pendingTimeouts = LinkedBlockingQueue<ScreenTimeout>()

    private val desiredScreenTimeouts = mutableListOf<ScreenTimeout>()

    fun getDesiredScreenTimeout(currentTimeout: ScreenTimeout): ScreenTimeout? {
        synchronized(desiredScreenTimeouts) {
            return if (desiredScreenTimeouts.contains(currentTimeout)) {
                desiredScreenTimeouts.removeUntil(currentTimeout)
                currentTimeout
            } else {
                desiredScreenTimeouts.clear()
                null
            }
        }
    }

    /**
     * [adoptionWait] caps how long each drained request polls for the system to reflect the
     * written value (see [SystemScreenTimeoutController.applyDesiredScreenTimeout]). The current
     * caller's budget applies to any request it drains for others; that only shortens how long
     * the adopted-report is polled, never the app-initiated marker recording.
     */
    suspend fun setDesiredScreenTimeout(
        timeout: ScreenTimeout,
        systemScreenTimeoutController: SystemScreenTimeoutController,
        adoptionWait: Duration,
    ): Boolean {
        return withContext(defaultDispatchers) {
            if (pendingTimeouts.lastOrNull() != timeout) {
                pendingTimeouts.add(timeout)
            }

            screenTimeoutProcessingLock.withLock {
                while (pendingTimeouts.isNotEmpty()) {
                    val requestedTimeout = pendingTimeouts.poll()

                    if (requestedTimeout != null) {
                        // Record the desired value before the system write, so the monitor worker
                        // can never observe the change ahead of its "app-initiated" marker.
                        synchronized(desiredScreenTimeouts) { desiredScreenTimeouts.add(requestedTimeout) }
                        systemScreenTimeoutController.setSystemScreenTimeout(requestedTimeout)

                        withTimeoutOrNull(adoptionWait) {
                            while (requestedTimeout != systemScreenTimeoutController.getSystemScreenTimeout()) {
                                delay(100.milliseconds)
                            }
                        }
                    }
                }
            }

            // Report whether the system actually adopted the requested value: some OEM
            // ROMs accept the write (putInt returns true) but silently keep the old value.
            systemScreenTimeoutController.getSystemScreenTimeout() == timeout
        }
    }
}
