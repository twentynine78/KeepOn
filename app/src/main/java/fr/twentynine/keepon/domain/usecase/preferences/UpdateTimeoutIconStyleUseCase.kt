package fr.twentynine.keepon.domain.usecase.preferences

import fr.twentynine.keepon.domain.gateway.AppComponentsUpdater
import fr.twentynine.keepon.domain.gateway.MemoryCacheManager
import fr.twentynine.keepon.domain.model.TimeoutIconStyle
import fr.twentynine.keepon.domain.repository.UiPreferencesRepository
import javax.inject.Inject

/**
 * Persists a new timeout-icon style. Clears the in-memory icon cache first so the
 * icons are regenerated with the new style, then refreshes the external surfaces
 * (QS tile + widget).
 */
class UpdateTimeoutIconStyleUseCase @Inject constructor(
    private val uiPreferencesRepository: UiPreferencesRepository,
    private val memoryCacheManager: MemoryCacheManager,
    private val appComponentsUpdater: AppComponentsUpdater,
) {
    suspend operator fun invoke(timeoutIconStyle: TimeoutIconStyle) {
        memoryCacheManager.clear()
        uiPreferencesRepository.setTimeoutIconStyle(timeoutIconStyle)
        appComponentsUpdater.requestUpdate()
    }
}
