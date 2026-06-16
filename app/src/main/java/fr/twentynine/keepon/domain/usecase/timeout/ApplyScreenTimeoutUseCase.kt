package fr.twentynine.keepon.domain.usecase.timeout

import fr.twentynine.keepon.domain.gateway.AppComponentsUpdater
import fr.twentynine.keepon.domain.gateway.DebugTracer
import fr.twentynine.keepon.domain.gateway.DevicePolicyController
import fr.twentynine.keepon.domain.gateway.SystemScreenTimeoutController
import fr.twentynine.keepon.domain.model.ScreenTimeout
import fr.twentynine.keepon.domain.repository.TimeoutPreferencesRepository
import javax.inject.Inject

/**
 * Core operation making [timeout] the active screen timeout: validates it against
 * the device policy, records the previous timeout, persists the current one, brings
 * the screen-off service in line, refreshes the external surfaces (tile + widget),
 * then writes the value to the system through the anti-collision queue.
 *
 * Internal building block shared by [UpdateSystemScreenTimeoutUseCase] and
 * [ResetSystemScreenTimeoutUseCase]; no-op when the value is not policy-allowed.
 *
 * @return true if the system adopted the value; false if it was rejected by the device
 * or not policy-allowed.
 */
class ApplyScreenTimeoutUseCase @Inject constructor(
    private val timeoutPreferencesRepository: TimeoutPreferencesRepository,
    private val devicePolicyController: DevicePolicyController,
    private val systemScreenTimeoutController: SystemScreenTimeoutController,
    private val appComponentsUpdater: AppComponentsUpdater,
    private val manageScreenOffServiceUseCase: ManageScreenOffServiceUseCase,
    private val tracer: DebugTracer,
) {
    suspend operator fun invoke(timeout: ScreenTimeout, forceUpdatePreviousTimeout: Boolean = false): Boolean {
        if (!devicePolicyController.isValidTimeout(timeout)) {
            return false
        }

        val currentTimeout = timeoutPreferencesRepository.getCurrentScreenTimeout()
        if (currentTimeout != timeout || forceUpdatePreviousTimeout) {
            timeoutPreferencesRepository.setPreviousScreenTimeout(currentTimeout)
        }

        // Update the stored current timeout first to prevent UI lag (it will be
        // reconciled by the monitor worker once the system applies the value).
        timeoutPreferencesRepository.setCurrentScreenTimeout(timeout)
        tracer.trace(TAG) { "optimistic store: current ${currentTimeout.value} -> ${timeout.value}" }
        manageScreenOffServiceUseCase()
        appComponentsUpdater.requestUpdate()

        val adopted = systemScreenTimeoutController.applyDesiredScreenTimeout(timeout)
        tracer.trace(TAG) { "apply result for ${timeout.value}: adopted=$adopted" }
        return adopted
    }

    private companion object {
        const val TAG = "Apply"
    }
}
