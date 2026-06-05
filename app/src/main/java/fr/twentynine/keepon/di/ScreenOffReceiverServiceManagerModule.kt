package fr.twentynine.keepon.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.twentynine.keepon.services.ScreenOffReceiverServiceManager
import fr.twentynine.keepon.services.ScreenOffReceiverServiceManagerImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ScreenOffReceiverServiceManagerModule {

    @Binds
    @Singleton
    abstract fun bindScreenOffReceiverServiceManager(impl: ScreenOffReceiverServiceManagerImpl): ScreenOffReceiverServiceManager
}
