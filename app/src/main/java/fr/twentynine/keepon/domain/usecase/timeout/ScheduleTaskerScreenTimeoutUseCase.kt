package fr.twentynine.keepon.domain.usecase.timeout

import fr.twentynine.keepon.domain.catalog.ScreenTimeoutCatalog
import fr.twentynine.keepon.domain.gateway.ScreenTimeoutScheduler
import fr.twentynine.keepon.domain.model.ScreenTimeout
import javax.inject.Inject

/**
 * Validates a screen timeout value received from a Tasker fire event and, when valid,
 * schedules the change. Returns whether the value was a known timeout (so the caller can
 * surface an error otherwise).
 */
class ScheduleTaskerScreenTimeoutUseCase @Inject constructor(
    private val screenTimeoutScheduler: ScreenTimeoutScheduler,
) {

    operator fun invoke(timeoutValue: Int): Boolean {
        val isValidScreenTimeout = timeoutValue != -1 &&
            (ScreenTimeoutCatalog.screenTimeouts + ScreenTimeoutCatalog.specialScreenTimeouts)
                .contains(ScreenTimeout(timeoutValue))

        if (isValidScreenTimeout) {
            screenTimeoutScheduler.schedule(timeoutValue, updatePreviousTimeout = true)
        }
        return isValidScreenTimeout
    }
}
