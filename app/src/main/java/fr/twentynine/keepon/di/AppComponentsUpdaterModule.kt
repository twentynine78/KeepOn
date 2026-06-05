package fr.twentynine.keepon.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.twentynine.keepon.util.AppComponentsUpdater
import fr.twentynine.keepon.util.AppComponentsUpdaterImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppComponentsUpdaterModule {

    @Binds
    @Singleton
    abstract fun bindAppComponentsUpdater(impl: AppComponentsUpdaterImpl): AppComponentsUpdater
}
