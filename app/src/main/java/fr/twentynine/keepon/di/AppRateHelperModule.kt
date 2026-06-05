package fr.twentynine.keepon.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.twentynine.keepon.util.AppRateHelper
import fr.twentynine.keepon.util.AppRateHelperImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppRateHelperModule {

    @Binds
    @Singleton
    abstract fun bindAppRateHelper(impl: AppRateHelperImpl): AppRateHelper
}
