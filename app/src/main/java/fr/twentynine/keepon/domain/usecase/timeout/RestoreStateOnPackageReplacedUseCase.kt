package fr.twentynine.keepon.domain.usecase.timeout

import fr.twentynine.keepon.domain.gateway.AppComponentsUpdater
import fr.twentynine.keepon.domain.gateway.ScreenOffReceiverServiceManager
import fr.twentynine.keepon.domain.repository.TimeoutPreferencesRepository
import fr.twentynine.keepon.domain.usecase.app.GetKeepOnStatusUseCase
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

/**
 * After the app package is replaced (update): when KeepOn is active and auto-reset is
 * enabled, restarts the screen-off service. Refreshes the external surfaces either way.
 */
class RestoreStateOnPackageReplacedUseCase @Inject constructor(
    private val timeoutPreferencesRepository: TimeoutPreferencesRepository,
    private val getKeepOnStatusUseCase: GetKeepOnStatusUseCase,
    private val screenOffReceiverServiceManager: ScreenOffReceiverServiceManager,
    private val appComponentsUpdater: AppComponentsUpdater,
) {
    suspend operator fun invoke() {
        val keepOnIsActive = getKeepOnStatusUseCase().firstOrNull() ?: false
        val resetWhenScreenOff = timeoutPreferencesRepository.getResetTimeoutWhenScreenOff()

        if (keepOnIsActive && resetWhenScreenOff) {
            screenOffReceiverServiceManager.startService()
        }
        appComponentsUpdater.requestUpdate()
    }
}
