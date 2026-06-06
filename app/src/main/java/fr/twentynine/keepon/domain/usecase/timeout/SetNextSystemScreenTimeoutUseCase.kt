package fr.twentynine.keepon.domain.usecase.timeout

import fr.twentynine.keepon.domain.gateway.DevicePolicyController
import fr.twentynine.keepon.domain.model.ScreenTimeout
import fr.twentynine.keepon.domain.repository.TimeoutPreferencesRepository
import javax.inject.Inject

/**
 * Cycles the system screen timeout to the next selected value (single entry point
 * for the QS tile and the widget). Computes the next timeout from the selected list
 * — always including the default and the current value, device-policy filtered — and
 * applies it through [UpdateSystemScreenTimeoutUseCase].
 *
 * @param currentTimeout the timeout to cycle from; defaults to the stored current one.
 */
class SetNextSystemScreenTimeoutUseCase @Inject constructor(
    private val timeoutPreferencesRepository: TimeoutPreferencesRepository,
    private val devicePolicyController: DevicePolicyController,
    private val updateSystemScreenTimeoutUseCase: UpdateSystemScreenTimeoutUseCase,
) {
    suspend operator fun invoke(currentTimeout: ScreenTimeout? = null) {
        val resolvedCurrentTimeout = currentTimeout ?: timeoutPreferencesRepository.getCurrentScreenTimeout()
        val nextTimeout = getNextSelectedScreenTimeout(resolvedCurrentTimeout)

        if (!devicePolicyController.isValidTimeout(nextTimeout)) {
            return
        }

        updateSystemScreenTimeoutUseCase(nextTimeout)
    }

    private suspend fun getNextSelectedScreenTimeout(currentTimeout: ScreenTimeout): ScreenTimeout {
        val screenTimeouts = timeoutPreferencesRepository.getSelectedScreenTimeouts().toMutableSet()
        val defaultScreenTimeout = timeoutPreferencesRepository.getDefaultScreenTimeout()

        screenTimeouts.addAll(
            devicePolicyController.removeNotAllowedScreenTimeout(
                listOf(defaultScreenTimeout, currentTimeout)
            )
        )

        val sortedScreenTimeouts = screenTimeouts.sortedBy { it.value }.distinct()
        if (sortedScreenTimeouts.isEmpty()) {
            return currentTimeout
        }

        val nextIndex = sortedScreenTimeouts.indexOfFirst { it.value == currentTimeout.value }.let { currentIndex ->
            if (currentIndex == -1 || currentIndex == sortedScreenTimeouts.size - 1) 0 else currentIndex + 1
        }
        return sortedScreenTimeouts[nextIndex]
    }
}
