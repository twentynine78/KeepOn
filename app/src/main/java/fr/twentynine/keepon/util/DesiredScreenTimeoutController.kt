package fr.twentynine.keepon.util

import fr.twentynine.keepon.data.model.ScreenTimeout
import fr.twentynine.keepon.util.extensions.removeUntil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

object DesiredScreenTimeoutController {
    private val WAIT_TIME_FOR_TIMEOUT_APPLIED = TimeUnit.SECONDS.toMillis(2)

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
    ) {
        withContext(defaultDispatchers) {
            pendingTimeouts.add(timeout)

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

                        withTimeout(WAIT_TIME_FOR_TIMEOUT_APPLIED) {
                            while (requestedTimeout != systemScreenTimeoutController.getSystemScreenTimeout()) {
                                delay(1)
                            }
                        }
                    }
                }
            }
        }
    }
}
