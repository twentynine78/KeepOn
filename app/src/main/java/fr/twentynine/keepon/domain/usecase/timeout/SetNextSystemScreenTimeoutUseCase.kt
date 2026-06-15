package fr.twentynine.keepon.domain.usecase.timeout

import fr.twentynine.keepon.domain.gateway.DevicePolicyController
import fr.twentynine.keepon.domain.model.ScreenTimeout
import fr.twentynine.keepon.domain.repository.TimeoutPreferencesRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Cycles the system screen timeout to the next selected value (single entry point
 * for the FAB, the QS tile and the widget). Computes the next timeout from the
 * selected list — always including the default and the current value, device-policy
 * filtered — and applies it through [UpdateSystemScreenTimeoutUseCase].
 *
 * Singleton with a mutex around the read-modify-write so concurrent taps — rapid
 * FAB taps or taps from two surfaces at once — each advance from the freshly
 * written current timeout instead of racing on the same read.
 */
@Singleton
class SetNextSystemScreenTimeoutUseCase @Inject constructor(
    private val timeoutPreferencesRepository: TimeoutPreferencesRepository,
    private val devicePolicyController: DevicePolicyController,
    private val updateSystemScreenTimeoutUseCase: UpdateSystemScreenTimeoutUseCase,
) {
    private val cycleMutex = Mutex()

    /** @param currentTimeout the timeout to cycle from; defaults to the stored current one. */
    suspend operator fun invoke(currentTimeout: ScreenTimeout? = null) {
        cycleMutex.withLock {
            val resolvedCurrentTimeout = currentTimeout ?: timeoutPreferencesRepository.getCurrentScreenTimeout()
            val nextTimeout = getNextSelectedScreenTimeout(resolvedCurrentTimeout)

            if (!devicePolicyController.isValidTimeout(nextTimeout)) {
                return
            }

            updateSystemScreenTimeoutUseCase(nextTimeout)
        }
    }

    private suspend fun getNextSelectedScreenTimeout(currentTimeout: ScreenTimeout): ScreenTimeout {
        val screenTimeouts = timeoutPreferencesRepository.getSelectedScreenTimeouts().toMutableSet()
        val defaultScreenTimeout = timeoutPreferencesRepository.getDefaultScreenTimeout()

        screenTimeouts.addAll(
            devicePolicyController.removeNotAllowedScreenTimeout(
                listOf(defaultScreenTimeout, currentTimeout)
            )
        )

        val sortedScreenTimeouts = screenTimeouts.sortedBy { it.value }
        if (sortedScreenTimeouts.isEmpty()) {
            return currentTimeout
        }

        val nextIndex = sortedScreenTimeouts.indexOfFirst { it.value == currentTimeout.value }.let { currentIndex ->
            if (currentIndex == -1 || currentIndex == sortedScreenTimeouts.size - 1) 0 else currentIndex + 1
        }
        return sortedScreenTimeouts[nextIndex]
    }
}
