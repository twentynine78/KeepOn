package fr.twentynine.keepon.domain.usecase.timeout

import fr.twentynine.keepon.domain.gateway.ScreenOffReceiverServiceManager
import fr.twentynine.keepon.domain.repository.TimeoutPreferencesRepository
import javax.inject.Inject

/**
 * Restores the default screen timeout on the system and stops the screen-off
 * service. Used when the screen turns off (auto-reset) and by the "Stop" action.
 */
class ResetSystemScreenTimeoutUseCase @Inject constructor(
    private val timeoutPreferencesRepository: TimeoutPreferencesRepository,
    private val applyScreenTimeoutUseCase: ApplyScreenTimeoutUseCase,
    private val screenOffReceiverServiceManager: ScreenOffReceiverServiceManager,
) {
    suspend operator fun invoke() {
        val defaultTimeout = timeoutPreferencesRepository.getDefaultScreenTimeout()
        applyScreenTimeoutUseCase(defaultTimeout)
        screenOffReceiverServiceManager.stopService()
    }
}
