package fr.twentynine.keepon.domain.usecase.timeout

import fr.twentynine.keepon.domain.gateway.ScreenOffReceiverServiceManager
import fr.twentynine.keepon.domain.gateway.SystemScreenTimeoutController
import fr.twentynine.keepon.domain.repository.TimeoutPreferencesRepository
import javax.inject.Inject
import kotlin.time.Duration

/**
 * Restores the default screen timeout on the system and stops the screen-off
 * service. Used when the screen turns off (auto-reset) and by the "Stop" action.
 * [adoptionWait] caps the system-adoption wait for callers running under a
 * tighter external budget (see [SystemScreenTimeoutController.applyDesiredScreenTimeout]).
 */
class ResetSystemScreenTimeoutUseCase @Inject constructor(
    private val timeoutPreferencesRepository: TimeoutPreferencesRepository,
    private val applyScreenTimeoutUseCase: ApplyScreenTimeoutUseCase,
    private val screenOffReceiverServiceManager: ScreenOffReceiverServiceManager,
) {
    suspend operator fun invoke(adoptionWait: Duration = SystemScreenTimeoutController.DEFAULT_ADOPTION_WAIT) {
        val defaultTimeout = timeoutPreferencesRepository.getDefaultScreenTimeout()
        applyScreenTimeoutUseCase(defaultTimeout, adoptionWait = adoptionWait)
        screenOffReceiverServiceManager.stopService()
    }
}
