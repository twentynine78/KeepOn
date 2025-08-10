package fr.twentynine.keepon.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import fr.twentynine.keepon.util.AppRateHelper
import fr.twentynine.keepon.util.AppRateHelperImpl

@Module
@InstallIn(SingletonComponent::class)
object AppRateHelperModule {

    @Provides
    fun provideAppRateHelper(@ApplicationContext context: Context): AppRateHelper {
        return AppRateHelperImpl(context)
    }
}
