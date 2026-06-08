package fr.twentynine.keepon.domain.usecase.preferences

import fr.twentynine.keepon.domain.model.IconTransitionAnimation
import fr.twentynine.keepon.domain.repository.UiPreferencesRepository
import javax.inject.Inject

/**
 * Persists the icon-change transition configuration (enabled flag + animation type).
 *
 * Unlike [UpdateTimeoutIconStyleUseCase] this neither clears the icon cache nor refreshes the
 * external surfaces: the setting only changes how a future timeout change is animated, not the
 * icon pixels.
 */
class UpdateIconTransitionAnimationUseCase @Inject constructor(
    private val uiPreferencesRepository: UiPreferencesRepository,
) {
    suspend operator fun invoke(iconTransitionAnimation: IconTransitionAnimation) {
        uiPreferencesRepository.setIconTransitionAnimation(iconTransitionAnimation)
    }
}
