package fr.twentynine.keepon.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.twentynine.keepon.data.local.PreferenceDataStoreHelper
import fr.twentynine.keepon.data.repo.UserPreferencesRepository
import fr.twentynine.keepon.data.repo.UserPreferencesRepositoryImpl
import fr.twentynine.keepon.services.ScreenOffReceiverServiceManager
import fr.twentynine.keepon.util.DevicePolicyManagerHelper
import fr.twentynine.keepon.util.SystemScreenTimeoutController

@Module
@InstallIn(SingletonComponent::class)
object UserPreferencesRepositoryModule {

    @Provides
    fun provideUserPreferencesRepository(
        preferenceDataStoreHelper: PreferenceDataStoreHelper,
        systemScreenTimeoutController: dagger.Lazy<SystemScreenTimeoutController>,
        devicePolicyManagerHelper: dagger.Lazy<DevicePolicyManagerHelper>,
        screenOffReceiverServiceManager: dagger.Lazy<ScreenOffReceiverServiceManager>,
    ): UserPreferencesRepository {
        return UserPreferencesRepositoryImpl(
            preferenceDataStoreHelper,
            systemScreenTimeoutController,
            devicePolicyManagerHelper,
            screenOffReceiverServiceManager
        )
    }
}
