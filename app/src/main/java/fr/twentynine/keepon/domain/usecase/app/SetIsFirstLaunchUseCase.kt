package fr.twentynine.keepon.domain.usecase.app

import fr.twentynine.keepon.domain.repository.AppPreferencesRepository
import javax.inject.Inject

class SetIsFirstLaunchUseCase @Inject constructor(
    private val appPreferencesRepository: AppPreferencesRepository,
) {
    suspend operator fun invoke(isFirstLaunch: Boolean) {
        appPreferencesRepository.setIsFirstLaunch(isFirstLaunch)
    }
}
