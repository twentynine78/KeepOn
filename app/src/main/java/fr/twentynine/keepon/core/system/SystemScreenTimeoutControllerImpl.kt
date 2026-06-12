package fr.twentynine.keepon.core.system

import android.content.Context
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import fr.twentynine.keepon.core.system.timeout.DesiredScreenTimeoutController
import fr.twentynine.keepon.domain.model.ScreenTimeout
import fr.twentynine.keepon.domain.gateway.DebugTracer
import fr.twentynine.keepon.domain.gateway.SystemScreenTimeoutController
import javax.inject.Inject
import kotlin.time.measureTimedValue

/**
 * Reads and writes the system `SCREEN_OFF_TIMEOUT` setting (requires WRITE_SETTINGS). App-initiated
 * changes go through [DesiredScreenTimeoutController] so the monitor worker can tell them apart from
 * the user changing the timeout in system settings. Reads fall back to a 1-minute default.
 */
class SystemScreenTimeoutControllerImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val tracer: DebugTracer,
) : SystemScreenTimeoutController {

    private val contentResolver by lazy { context.contentResolver }

    override fun getSystemScreenTimeout(): ScreenTimeout {
        return ScreenTimeout(
            Settings.System.getInt(
                contentResolver,
                Settings.System.SCREEN_OFF_TIMEOUT,
                DEFAULT_SCREEN_TIMEOUT
            )
        )
    }

    override fun setSystemScreenTimeout(timeout: ScreenTimeout) {
        tracer.trace(TAG) { "writing SCREEN_OFF_TIMEOUT = ${timeout.value}" }
        val success = Settings.System.putInt(
            contentResolver,
            Settings.System.SCREEN_OFF_TIMEOUT,
            timeout.value
        )
        if (!success) {
            tracer.trace(TAG) { "putInt rejected for ${timeout.value} (WRITE_SETTINGS missing?)" }
            throw SecurityException("Failed to write SCREEN_OFF_TIMEOUT — WRITE_SETTINGS permission may be missing or revoked")
        }
    }

    override suspend fun applyDesiredScreenTimeout(timeout: ScreenTimeout): Boolean {
        // The desired-timeout queue is an internal detail of this system-write gateway:
        // it records the app-initiated intent and waits for the system to apply it.
        val (applied, elapsed) = measureTimedValue {
            DesiredScreenTimeoutController.setDesiredScreenTimeout(timeout, this)
        }
        tracer.trace(TAG) {
            "apply ${timeout.value}: ${if (applied) "adopted" else "NOT adopted (OEM rejection?)"} after $elapsed"
        }
        return applied
    }

    override fun consumeDesiredScreenTimeout(currentTimeout: ScreenTimeout): ScreenTimeout? {
        val desired = DesiredScreenTimeoutController.getDesiredScreenTimeout(currentTimeout)
        tracer.trace(TAG) {
            if (desired != null) "consume ${currentTimeout.value}: app-initiated"
            else "consume ${currentTimeout.value}: no desired pending -> external change"
        }
        return desired
    }

    companion object {
        private const val DEFAULT_SCREEN_TIMEOUT = 60000
        private const val TAG = "Timeout"
    }
}
