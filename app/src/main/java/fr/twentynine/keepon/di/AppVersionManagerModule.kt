package fr.twentynine.keepon.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.twentynine.keepon.util.AppVersionManager
import fr.twentynine.keepon.util.AppVersionManagerImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppVersionManagerModule {

    @Binds
    @Singleton
    abstract fun bindAppVersionManager(impl: AppVersionManagerImpl): AppVersionManager
}
