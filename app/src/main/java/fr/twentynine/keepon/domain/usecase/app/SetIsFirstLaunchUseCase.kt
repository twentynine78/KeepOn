package fr.twentynine.keepon.domain.usecase.app

import fr.twentynine.keepon.domain.repository.AppPreferencesRepository
import javax.inject.Inject

/** Persists the first-launch flag (cleared once the user has been through the initial flow). */
class SetIsFirstLaunchUseCase @Inject constructor(
    private val appPreferencesRepository: AppPreferencesRepository,
) {
    suspend operator fun invoke(isFirstLaunch: Boolean) {
        appPreferencesRepository.setIsFirstLaunch(isFirstLaunch)
    }
}
