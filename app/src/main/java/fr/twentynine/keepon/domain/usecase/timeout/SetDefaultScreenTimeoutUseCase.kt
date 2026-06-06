package fr.twentynine.keepon.domain.usecase.timeout

import fr.twentynine.keepon.domain.gateway.AppComponentsUpdater
import fr.twentynine.keepon.domain.gateway.ScreenOffReceiverServiceManager
import fr.twentynine.keepon.domain.model.ScreenTimeout
import fr.twentynine.keepon.domain.repository.TimeoutPreferencesRepository
import javax.inject.Inject

/**
 * Changes the default screen timeout. Persists the new default and, if it now equals
 * the current timeout, stops the screen-off service. When the current timeout was
 * sitting on the old default, it is moved along to the new default and applied to the
 * system (via [UpdateSystemScreenTimeoutUseCase], which also adds it to the selected
 * list when auto-reset is off), recording the old value as the previous timeout.
 */
class SetDefaultScreenTimeoutUseCase @Inject constructor(
    private val timeoutPreferencesRepository: TimeoutPreferencesRepository,
    private val screenOffReceiverServiceManager: ScreenOffReceiverServiceManager,
    private val updateSystemScreenTimeoutUseCase: UpdateSystemScreenTimeoutUseCase,
    private val appComponentsUpdater: AppComponentsUpdater,
) {
    suspend operator fun invoke(newDefaultTimeout: ScreenTimeout) {
        val defaultTimeout = timeoutPreferencesRepository.getDefaultScreenTimeout()
        val currentTimeout = timeoutPreferencesRepository.getCurrentScreenTimeout()

        if (newDefaultTimeout != defaultTimeout) {
            timeoutPreferencesRepository.setDefaultScreenTimeout(newDefaultTimeout)
            if (newDefaultTimeout == currentTimeout) {
                screenOffReceiverServiceManager.stopService()
            }

            if (defaultTimeout == currentTimeout) {
                // The current timeout was sitting on the old default: carry it over to
                // the new one, keeping the old value as the previous timeout.
                timeoutPreferencesRepository.setPreviousScreenTimeout(currentTimeout)
                timeoutPreferencesRepository.setCurrentScreenTimeout(newDefaultTimeout)
                updateSystemScreenTimeoutUseCase(newDefaultTimeout)
            }
        }

        appComponentsUpdater.requestUpdate()
    }
}
