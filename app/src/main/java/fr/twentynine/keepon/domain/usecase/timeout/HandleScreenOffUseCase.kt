package fr.twentynine.keepon.domain.usecase.timeout

import fr.twentynine.keepon.domain.gateway.TaskerEventNotifier
import fr.twentynine.keepon.domain.model.TaskerEventType
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

/**
 * Handles the screen turning off: resets the system screen timeout to the default, then — only if
 * the reset completed without throwing — emits the Tasker "reset on screen off" plug-in event.
 * The notification "Stop KeepOn" path calls [ResetSystemScreenTimeoutUseCase] directly and does
 * not emit the event.
 */
class HandleScreenOffUseCase @Inject constructor(
    private val resetSystemScreenTimeoutUseCase: ResetSystemScreenTimeoutUseCase,
    private val taskerEventNotifier: TaskerEventNotifier,
) {
    suspend operator fun invoke() {
        resetSystemScreenTimeoutUseCase(adoptionWait = SCREEN_OFF_ADOPTION_WAIT)
        taskerEventNotifier.notifyEvent(TaskerEventType.RESET_ON_SCREEN_OFF)
    }

    private companion object {
        // The whole screen-off handling runs inside the receiver's 5-second goAsync budget.
        // Capping the system-adoption wait below it guarantees the reset returns (adopted or
        // not) with budget left for the event emission — otherwise a non-adopting device would
        // exhaust the outer budget first and silently cancel notifyEvent.
        val SCREEN_OFF_ADOPTION_WAIT = 2.seconds
    }
}
