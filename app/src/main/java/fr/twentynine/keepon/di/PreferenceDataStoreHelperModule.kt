package fr.twentynine.keepon.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.twentynine.keepon.data.local.PreferenceDataStoreHelper
import fr.twentynine.keepon.data.local.PreferenceDataStoreHelperImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PreferenceDataStoreHelperModule {

    @Binds
    @Singleton
    abstract fun bindPreferenceDataStoreHelper(impl: PreferenceDataStoreHelperImpl): PreferenceDataStoreHelper
}
