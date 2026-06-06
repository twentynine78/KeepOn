package fr.twentynine.keepon.domain.usecase.preferences

import fr.twentynine.keepon.domain.repository.UiPreferencesRepository
import javax.inject.Inject

/**
 * Records whether the Quick Settings tile has been added by the user.
 */
class SetQSTileAddedUseCase @Inject constructor(
    private val uiPreferencesRepository: UiPreferencesRepository,
) {
    suspend operator fun invoke(isAdded: Boolean) {
        uiPreferencesRepository.setQSTileAdded(isAdded)
    }
}
