package fr.twentynine.keepon.domain.usecase.preferences

import fr.twentynine.keepon.domain.gateway.ScreenOffReceiverServiceManager
import fr.twentynine.keepon.domain.repository.TimeoutPreferencesRepository
import javax.inject.Inject

/**
 * Toggles the "reset timeout when screen off" preference and reconciles the side
 * effects: when disabled, the screen-off service is stopped and the current timeout
 * is promoted to the default; when enabled, the service is started if the current
 * timeout already differs from the default. The flag is persisted last.
 */
class SetResetTimeoutWhenScreenOffUseCase @Inject constructor(
    private val timeoutPreferencesRepository: TimeoutPreferencesRepository,
    private val screenOffReceiverServiceManager: ScreenOffReceiverServiceManager,
) {
    suspend operator fun invoke(resetWhenScreenOff: Boolean) {
        if (!resetWhenScreenOff) {
            screenOffReceiverServiceManager.stopService()
            timeoutPreferencesRepository.setDefaultScreenTimeout(
                timeoutPreferencesRepository.getCurrentScreenTimeout()
            )
        } else if (timeoutPreferencesRepository.getCurrentScreenTimeout() != timeoutPreferencesRepository.getDefaultScreenTimeout()) {
            screenOffReceiverServiceManager.startService()
        }

        timeoutPreferencesRepository.setResetTimeoutWhenScreenOff(resetWhenScreenOff)
    }
}
