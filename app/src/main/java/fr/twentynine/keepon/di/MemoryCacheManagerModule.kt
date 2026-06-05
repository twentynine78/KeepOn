package fr.twentynine.keepon.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.twentynine.keepon.util.coil.MemoryCacheManager
import fr.twentynine.keepon.util.coil.MemoryCacheManagerImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MemoryCacheManagerModule {

    @Binds
    @Singleton
    abstract fun bindMemoryCacheManager(impl: MemoryCacheManagerImpl): MemoryCacheManager
}
