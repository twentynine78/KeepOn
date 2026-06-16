package fr.twentynine.keepon.domain.usecase.timeout

import fr.twentynine.keepon.domain.gateway.AppComponentsUpdater
import fr.twentynine.keepon.domain.gateway.DebugTracer
import fr.twentynine.keepon.domain.gateway.SystemScreenTimeoutController
import fr.twentynine.keepon.domain.repository.TimeoutPreferencesRepository
import javax.inject.Inject

/**
 * Reconciles the stored state with the live system screen timeout (run by the monitor
 * worker when the system value changes). If the change was app-initiated (it matches a
 * pending desired timeout) the stored state is left untouched — [ApplyScreenTimeoutUseCase]
 * already wrote the current value optimistically, and reconciling it from this (possibly
 * stale, monitor-coalesced) system read would clobber a value a later rapid tap may already
 * have advanced past. Otherwise the user changed it from the system settings, so the new
 * value becomes both the default and the current timeout (recording the old one as previous).
 * Either way the screen-off service is brought in line and the external surfaces are refreshed.
 */
class SynchronizeSystemTimeoutUseCase @Inject constructor(
    private val systemScreenTimeoutController: SystemScreenTimeoutController,
    private val timeoutPreferencesRepository: TimeoutPreferencesRepository,
    private val manageScreenOffServiceUseCase: ManageScreenOffServiceUseCase,
    private val appComponentsUpdater: AppComponentsUpdater,
    private val tracer: DebugTracer,
) {
    suspend operator fun invoke() {
        val currentSystemTimeout = systemScreenTimeoutController.getSystemScreenTimeout()
        tracer.trace(TAG) { "monitor read system=${currentSystemTimeout.value}" }
        val desiredTimeout = systemScreenTimeoutController.consumeDesiredScreenTimeout(currentSystemTimeout)

        // A mismatch means the change did not come from the app (the user edited the timeout
        // in the system settings): adopt the new system value as both the default and the
        // current timeout. When it matches, the change is app-initiated and the optimistic
        // write in ApplyScreenTimeoutUseCase already owns the current value — reconciling it
        // here would clobber a value a later rapid tap may already have advanced past this
        // now-stale system read.
        if (desiredTimeout != currentSystemTimeout) {
            val storedCurrentTimeout = timeoutPreferencesRepository.getCurrentScreenTimeout()
            tracer.trace(TAG) {
                "external change: adopting ${currentSystemTimeout.value} as default + current (was ${storedCurrentTimeout.value})"
            }
            timeoutPreferencesRepository.setDefaultScreenTimeout(currentSystemTimeout)
            if (storedCurrentTimeout != currentSystemTimeout) {
                timeoutPreferencesRepository.setPreviousScreenTimeout(storedCurrentTimeout)
            }
            timeoutPreferencesRepository.setCurrentScreenTimeout(currentSystemTimeout)
        } else {
            tracer.trace(TAG) { "app-initiated change to ${currentSystemTimeout.value}: stored current left to the optimistic write" }
        }

        manageScreenOffServiceUseCase()
        appComponentsUpdater.requestUpdate()
    }

    private companion object {
        const val TAG = "Sync"
    }
}
