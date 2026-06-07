package fr.twentynine.keepon.domain.usecase.timeout

import fr.twentynine.keepon.domain.gateway.AppComponentsUpdater
import fr.twentynine.keepon.domain.gateway.ScreenTimeoutScheduler
import fr.twentynine.keepon.domain.model.SpecialScreenTimeoutType
import fr.twentynine.keepon.domain.repository.TimeoutPreferencesRepository
import javax.inject.Inject

/**
 * On boot: when auto-reset is enabled and the current timeout differs from the default,
 * schedules a return to the default timeout. Refreshes the external surfaces either way.
 */
class ScheduleDefaultTimeoutOnBootUseCase @Inject constructor(
    private val timeoutPreferencesRepository: TimeoutPreferencesRepository,
    private val screenTimeoutScheduler: ScreenTimeoutScheduler,
    private val appComponentsUpdater: AppComponentsUpdater,
) {
    suspend operator fun invoke() {
        if (timeoutPreferencesRepository.getResetTimeoutWhenScreenOff()) {
            val currentScreenTimeout = timeoutPreferencesRepository.getCurrentScreenTimeout()
            val defaultScreenTimeout = timeoutPreferencesRepository.getDefaultScreenTimeout()

            if (currentScreenTimeout != defaultScreenTimeout) {
                screenTimeoutScheduler.schedule(SpecialScreenTimeoutType.DEFAULT_SCREEN_TIMEOUT_TYPE.value)
            }
        }
        appComponentsUpdater.requestUpdate()
    }
}
