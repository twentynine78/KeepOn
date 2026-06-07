package fr.twentynine.keepon.core.system.timeout

import fr.twentynine.keepon.domain.gateway.SystemScreenTimeoutController
import fr.twentynine.keepon.domain.model.ScreenTimeout
import fr.twentynine.keepon.core.util.removeUntil
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.LinkedBlockingQueue

object DesiredScreenTimeoutController {
    // Max time to wait for the system to reflect the written value before considering it
    // rejected. Successful writes converge in ~100 ms, so this ceiling is only reached on
    // slow or non-applying (OEM-restricted) devices.
    private val WAIT_TIME_FOR_TIMEOUT_APPLIED = 5.seconds

    private val defaultDispatchers = Dispatchers.Default
    private val screenTimeoutProcessingLock = Mutex()

    @Volatile
    private var pendingTimeouts = LinkedBlockingQueue<ScreenTimeout>()

    @Volatile
    private var desiredScreenTimeouts = mutableListOf<ScreenTimeout>()

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

    suspend fun setDesiredScreenTimeout(
        timeout: ScreenTimeout,
        systemScreenTimeoutController: SystemScreenTimeoutController,
    ): Boolean {
        return withContext(defaultDispatchers) {
            if (pendingTimeouts.lastOrNull() != timeout) {
                pendingTimeouts.add(timeout)
            }

            screenTimeoutProcessingLock.withLock {
                while (pendingTimeouts.isNotEmpty()) {
                    val requestedTimeout = pendingTimeouts.poll()

                    if (requestedTimeout != null) {
                        coroutineScope {
                            val deferreds = listOf(
                                async {
                                    synchronized(desiredScreenTimeouts) { desiredScreenTimeouts.add(requestedTimeout) }
                                },
                                async {
                                    systemScreenTimeoutController.setSystemScreenTimeout(requestedTimeout)
                                }
                            )
                            deferreds.awaitAll()
                        }

                        withTimeoutOrNull(WAIT_TIME_FOR_TIMEOUT_APPLIED) {
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
