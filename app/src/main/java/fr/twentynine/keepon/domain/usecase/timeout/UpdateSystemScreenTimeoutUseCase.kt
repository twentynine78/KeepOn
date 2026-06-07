package fr.twentynine.keepon.domain.usecase.timeout

import fr.twentynine.keepon.domain.gateway.DevicePolicyController
import fr.twentynine.keepon.domain.gateway.UserNotifier
import fr.twentynine.keepon.domain.model.ScreenTimeout
import fr.twentynine.keepon.domain.model.SpecialScreenTimeoutType
import fr.twentynine.keepon.domain.repository.TimeoutPreferencesRepository
import javax.inject.Inject

/**
 * Sets a new system screen timeout. Resolves the special "default"/"previous"
 * sentinels to their concrete value, applies it via [ApplyScreenTimeoutUseCase],
 * and — when auto-reset is off — promotes the new value to the default (adding the
 * outgoing current timeout to the selected list when it was the default).
 *
 * No-op when the resolved value is not allowed by the device policy.
 */
class UpdateSystemScreenTimeoutUseCase @Inject constructor(
    private val timeoutPreferencesRepository: TimeoutPreferencesRepository,
    private val devicePolicyController: DevicePolicyController,
    private val applyScreenTimeoutUseCase: ApplyScreenTimeoutUseCase,
    private val revertScreenTimeoutChangeUseCase: RevertScreenTimeoutChangeUseCase,
    private val userNotifier: UserNotifier,
) {
    suspend operator fun invoke(newTimeout: ScreenTimeout, forceUpdatePreviousTimeout: Boolean = false) {
        val defaultTimeout = timeoutPreferencesRepository.getDefaultScreenTimeout()
        val currentTimeout = timeoutPreferencesRepository.getCurrentScreenTimeout()
        val previousTimeout = timeoutPreferencesRepository.getPreviousScreenTimeout()

        val timeout = when (newTimeout.value) {
            SpecialScreenTimeoutType.DEFAULT_SCREEN_TIMEOUT_TYPE.value -> defaultTimeout
            SpecialScreenTimeoutType.PREVIOUS_SCREEN_TIMEOUT_TYPE.value -> previousTimeout
            else -> newTimeout
        }

        if (!devicePolicyController.isValidTimeout(timeout)) {
            return
        }

        val applied = applyScreenTimeoutUseCase(timeout, forceUpdatePreviousTimeout)
        if (!applied) {
            // The device silently refused the write: restore the stored state to the real
            // current value and tell the user, instead of leaving the tile/widget showing
            // a value that never took effect.
            revertScreenTimeoutChangeUseCase(currentTimeout)
            userNotifier.notifyScreenTimeoutNotApplied()
            return
        }

        updateDefaultScreenTimeoutIfNoResetTimeout(currentTimeout, defaultTimeout, newTimeout)
    }

    private suspend fun updateDefaultScreenTimeoutIfNoResetTimeout(
        currentTimeout: ScreenTimeout,
        defaultTimeout: ScreenTimeout,
        newTimeout: ScreenTimeout,
    ) {
        if (timeoutPreferencesRepository.getResetTimeoutWhenScreenOff()) {
            return
        }

        // Add the outgoing current timeout to the selected list if it was the default.
        if (currentTimeout == defaultTimeout) {
            val selectedTimeouts = timeoutPreferencesRepository.getSelectedScreenTimeouts()
            if (!selectedTimeouts.contains(currentTimeout)) {
                timeoutPreferencesRepository.setSelectedScreenTimeouts(selectedTimeouts.plus(currentTimeout))
            }
        }

        timeoutPreferencesRepository.setDefaultScreenTimeout(newTimeout)
    }
}
