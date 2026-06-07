package fr.twentynine.keepon.core.service

import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Shared running state of [ScreenOffReceiverService]: set by the service itself and read by
 * [ScreenOffReceiverServiceManagerImpl] to decide whether to start/stop it. A single injected
 * singleton instead of static mutable state, so both collaborators observe the same value.
 */
@Singleton
class ScreenOffServiceState @Inject constructor() {
    private val running = AtomicBoolean(false)

    var isRunning: Boolean
        get() = running.get()
        set(value) {
            running.set(value)
        }
}
