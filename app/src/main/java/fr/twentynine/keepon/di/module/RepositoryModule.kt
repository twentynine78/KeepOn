package fr.twentynine.keepon.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.twentynine.keepon.data.local.PreferenceDataStoreHelper
import fr.twentynine.keepon.data.local.PreferenceDataStoreHelperImpl
import fr.twentynine.keepon.data.repo.UserPreferencesRepository
import fr.twentynine.keepon.data.repo.UserPreferencesRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(impl: UserPreferencesRepositoryImpl): UserPreferencesRepository

    @Binds
    @Singleton
    abstract fun bindPreferenceDataStoreHelper(impl: PreferenceDataStoreHelperImpl): PreferenceDataStoreHelper
}
