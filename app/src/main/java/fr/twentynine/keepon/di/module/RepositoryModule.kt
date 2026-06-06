package fr.twentynine.keepon.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.twentynine.keepon.data.local.PreferenceDataStoreHelper
import fr.twentynine.keepon.data.local.PreferenceDataStoreHelperImpl
import fr.twentynine.keepon.data.migration.LegacyPreferencesRepository
import fr.twentynine.keepon.data.migration.LegacyPreferencesRepositoryImpl
import fr.twentynine.keepon.data.repository.MigratingAppPreferencesRepository
import fr.twentynine.keepon.data.repository.MigratingTimeoutPreferencesRepository
import fr.twentynine.keepon.data.repository.MigratingUiPreferencesRepository
import fr.twentynine.keepon.domain.repository.AppPreferencesRepository
import fr.twentynine.keepon.domain.repository.TimeoutPreferencesRepository
import fr.twentynine.keepon.domain.repository.UiPreferencesRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {

    @Binds
    @Singleton
    fun bindPreferenceDataStoreHelper(impl: PreferenceDataStoreHelperImpl): PreferenceDataStoreHelper

    @Binds
    @Singleton
    fun bindLegacyPreferencesRepository(impl: LegacyPreferencesRepositoryImpl): LegacyPreferencesRepository

    @Binds
    @Singleton
    fun bindAppPreferencesRepository(impl: MigratingAppPreferencesRepository): AppPreferencesRepository

    @Binds
    @Singleton
    fun bindUiPreferencesRepository(impl: MigratingUiPreferencesRepository): UiPreferencesRepository

    @Binds
    @Singleton
    fun bindTimeoutPreferencesRepository(impl: MigratingTimeoutPreferencesRepository): TimeoutPreferencesRepository
}
