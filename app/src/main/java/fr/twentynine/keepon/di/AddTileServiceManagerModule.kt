package fr.twentynine.keepon.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.twentynine.keepon.util.AddTileServiceManager
import fr.twentynine.keepon.util.AddTileServiceManagerImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AddTileServiceManagerModule {

    @Binds
    @Singleton
    abstract fun bindAddTileServiceManager(impl: AddTileServiceManagerImpl): AddTileServiceManager
}
