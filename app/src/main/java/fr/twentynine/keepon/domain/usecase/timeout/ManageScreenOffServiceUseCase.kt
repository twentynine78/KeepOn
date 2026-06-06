package fr.twentynine.keepon.domain.usecase.timeout

import fr.twentynine.keepon.domain.gateway.ScreenOffReceiverServiceManager
import fr.twentynine.keepon.domain.repository.TimeoutPreferencesRepository
import javax.inject.Inject

/**
 * Starts or stops the foreground "screen off" service to match the current state:
 * it runs only when the user opted to reset on screen off AND the current timeout
 * differs from the default one. Otherwise it is stopped.
 */
class ManageScreenOffServiceUseCase @Inject constructor(
    private val timeoutPreferencesRepository: TimeoutPreferencesRepository,
    private val screenOffReceiverServiceManager: ScreenOffReceiverServiceManager,
) {
    suspend operator fun invoke() {
        val currentTimeout = timeoutPreferencesRepository.getCurrentScreenTimeout()
        val defaultTimeout = timeoutPreferencesRepository.getDefaultScreenTimeout()
        val resetWhenScreenOff = timeoutPreferencesRepository.getResetTimeoutWhenScreenOff()

        if (resetWhenScreenOff && currentTimeout != defaultTimeout) {
            screenOffReceiverServiceManager.startService()
        } else {
            screenOffReceiverServiceManager.stopService()
        }
    }
}
