package fr.twentynine.keepon.domain.usecase.timeout

import fr.twentynine.keepon.domain.gateway.AppComponentsUpdater
import fr.twentynine.keepon.domain.gateway.SystemScreenTimeoutController
import fr.twentynine.keepon.domain.repository.TimeoutPreferencesRepository
import javax.inject.Inject

/**
 * Reconciles the stored state with the live system screen timeout (run by the monitor
 * worker when the system value changes). If the change was app-initiated (it matches a
 * pending desired timeout) the base is left as-is; otherwise the user changed it from
 * the system settings, so the new value becomes the default. The stored current value
 * is updated either way (recording the old one as previous), the screen-off service is
 * brought in line, and the external surfaces are refreshed.
 */
class SynchronizeSystemTimeoutUseCase @Inject constructor(
    private val systemScreenTimeoutController: SystemScreenTimeoutController,
    private val timeoutPreferencesRepository: TimeoutPreferencesRepository,
    private val manageScreenOffServiceUseCase: ManageScreenOffServiceUseCase,
    private val appComponentsUpdater: AppComponentsUpdater,
) {
    suspend operator fun invoke() {
        val currentSystemTimeout = systemScreenTimeoutController.getSystemScreenTimeout()
        val desiredTimeout = systemScreenTimeoutController.consumeDesiredScreenTimeout(currentSystemTimeout)

        // A mismatch means the change did not come from the app (system settings):
        // adopt the new system value as the default.
        if (desiredTimeout != currentSystemTimeout) {
            timeoutPreferencesRepository.setDefaultScreenTimeout(currentSystemTimeout)
        }

        val storedCurrentTimeout = timeoutPreferencesRepository.getCurrentScreenTimeout()
        if (storedCurrentTimeout != currentSystemTimeout) {
            timeoutPreferencesRepository.setPreviousScreenTimeout(storedCurrentTimeout)
        }
        timeoutPreferencesRepository.setCurrentScreenTimeout(currentSystemTimeout)

        manageScreenOffServiceUseCase()
        appComponentsUpdater.requestUpdate()
    }
}
