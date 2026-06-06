package fr.twentynine.keepon.domain.usecase.app

import fr.twentynine.keepon.domain.repository.AppPreferencesRepository
import javax.inject.Inject

/**
 * Increments the stored app-launch counter and, once past the first couple of
 * launches, clears the "first launch" flag (so the onboarding is shown only early on).
 */
class IncrementAppLaunchCountUseCase @Inject constructor(
    private val appPreferencesRepository: AppPreferencesRepository,
) {
    suspend operator fun invoke() {
        val currentCount = appPreferencesRepository.getAppLaunchCount()
        appPreferencesRepository.setAppLaunchCount(currentCount + 1)

        if (currentCount > 1L) {
            appPreferencesRepository.setIsFirstLaunch(false)
        }
    }
}
