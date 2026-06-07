package fr.twentynine.keepon.domain.usecase.timeout

import fr.twentynine.keepon.domain.model.ScreenTimeout
import fr.twentynine.keepon.domain.repository.TimeoutPreferencesRepository
import javax.inject.Inject

/**
 * Whether the screen timeout can be cycled — i.e. there is at least one selected timeout
 * (the default is always considered selectable) different from the current one. When false,
 * the QS tile / widget route to the app instead of cycling.
 *
 * Single source of truth: the QS tile calls [CanCycleScreenTimeoutUseCase]; the widget state
 * producer calls [canCycleScreenTimeout] directly with the values it already loaded.
 */
fun canCycleScreenTimeout(
    selectedTimeouts: List<ScreenTimeout>,
    defaultTimeout: ScreenTimeout,
    currentTimeout: ScreenTimeout,
): Boolean {
    val timeoutsWithDefault = if (selectedTimeouts.contains(defaultTimeout)) {
        selectedTimeouts
    } else {
        listOf(defaultTimeout) + selectedTimeouts
    }
    return timeoutsWithDefault.any { screenTimeout -> screenTimeout != currentTimeout }
}

class CanCycleScreenTimeoutUseCase @Inject constructor(
    private val timeoutPreferencesRepository: TimeoutPreferencesRepository,
) {
    suspend operator fun invoke(): Boolean = canCycleScreenTimeout(
        selectedTimeouts = timeoutPreferencesRepository.getSelectedScreenTimeouts(),
        defaultTimeout = timeoutPreferencesRepository.getDefaultScreenTimeout(),
        currentTimeout = timeoutPreferencesRepository.getCurrentScreenTimeout(),
    )
}
