package fr.twentynine.keepon.domain.usecase.timeout

import fr.twentynine.keepon.domain.model.ScreenTimeout
import fr.twentynine.keepon.domain.repository.TimeoutPreferencesRepository
import javax.inject.Inject

/**
 * Adds or removes [screenTimeout] from the user's selected timeouts. The default
 * timeout is always implicitly selected and cannot be toggled off, so it is ignored.
 */
class ToggleScreenTimeoutSelectionUseCase @Inject constructor(
    private val timeoutPreferencesRepository: TimeoutPreferencesRepository,
) {
    suspend operator fun invoke(screenTimeout: ScreenTimeout) {
        val defaultScreenTimeout = timeoutPreferencesRepository.getDefaultScreenTimeout()
        if (screenTimeout.value == defaultScreenTimeout.value) {
            return
        }

        val currentSelection = timeoutPreferencesRepository.getSelectedScreenTimeouts()
        timeoutPreferencesRepository.setSelectedScreenTimeouts(
            if (currentSelection.contains(screenTimeout)) {
                currentSelection.minus(screenTimeout)
            } else {
                currentSelection.plus(screenTimeout)
            }
        )
    }
}
