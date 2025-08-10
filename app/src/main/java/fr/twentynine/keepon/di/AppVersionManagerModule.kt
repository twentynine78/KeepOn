package fr.twentynine.keepon.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import fr.twentynine.keepon.data.repo.UserPreferencesRepository
import fr.twentynine.keepon.util.AppVersionManager
import fr.twentynine.keepon.util.AppVersionManagerImpl

@Module
@InstallIn(SingletonComponent::class)
object AppVersionManagerModule {

    @Provides
    fun provideAppVersionManager(
        userPreferencesRepository: UserPreferencesRepository,
        @ApplicationContext context: Context,
    ): AppVersionManager {
        return AppVersionManagerImpl(userPreferencesRepository, context)
    }
}
