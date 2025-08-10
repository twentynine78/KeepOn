package fr.twentynine.keepon.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import fr.twentynine.keepon.data.local.PreferenceDataStoreHelper
import fr.twentynine.keepon.data.local.PreferenceDataStoreHelperImpl

@Module
@InstallIn(SingletonComponent::class)
object PreferenceDataStoreHelperModule {

    @Provides
    fun providePreferenceDataStoreHelper(@ApplicationContext context: Context): PreferenceDataStoreHelper {
        return PreferenceDataStoreHelperImpl(context)
    }
}
