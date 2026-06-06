package fr.twentynine.keepon.domain.usecase.preferences

import fr.twentynine.keepon.domain.model.DismissedTips
import fr.twentynine.keepon.domain.repository.UiPreferencesRepository
import javax.inject.Inject

/**
 * Persists that the tip identified by [tipId] has been dismissed by the user.
 */
class DismissTipUseCase @Inject constructor(
    private val uiPreferencesRepository: UiPreferencesRepository,
) {
    suspend operator fun invoke(tipId: Int) {
        uiPreferencesRepository.setDismissedTip(DismissedTips(tipId))
    }
}
