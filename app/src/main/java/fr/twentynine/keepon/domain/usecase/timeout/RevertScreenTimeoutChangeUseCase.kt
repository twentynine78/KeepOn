package fr.twentynine.keepon.domain.usecase.timeout

import fr.twentynine.keepon.domain.gateway.AppComponentsUpdater
import fr.twentynine.keepon.domain.model.ScreenTimeout
import fr.twentynine.keepon.domain.repository.TimeoutPreferencesRepository
import javax.inject.Inject

/**
 * Reverts KeepOn's stored timeout state back to [currentTimeout] without touching the
 * system, then realigns the screen-off service and the external surfaces (tile + widget).
 *
 * Used when the system silently rejected an app-initiated write (some OEM ROMs accept
 * the call but keep the old value): it undoes the optimistic update performed by
 * [ApplyScreenTimeoutUseCase] so the surfaces stop showing a value that never took effect.
 */
class RevertScreenTimeoutChangeUseCase @Inject constructor(
    private val timeoutPreferencesRepository: TimeoutPreferencesRepository,
    private val manageScreenOffServiceUseCase: ManageScreenOffServiceUseCase,
    private val appComponentsUpdater: AppComponentsUpdater,
) {
    suspend operator fun invoke(currentTimeout: ScreenTimeout) {
        timeoutPreferencesRepository.setCurrentScreenTimeout(currentTimeout)
        manageScreenOffServiceUseCase()
        appComponentsUpdater.requestUpdate()
    }
}
