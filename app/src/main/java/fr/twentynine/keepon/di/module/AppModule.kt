package fr.twentynine.keepon.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.twentynine.keepon.util.AppRateManager
import fr.twentynine.keepon.util.AppRateManagerImpl
import fr.twentynine.keepon.util.migration.AppVersionManager
import fr.twentynine.keepon.util.migration.AppVersionManagerImpl
import fr.twentynine.keepon.util.StringResourceProvider
import fr.twentynine.keepon.util.StringResourceProviderImpl
import fr.twentynine.keepon.util.coil.MemoryCacheManager
import fr.twentynine.keepon.util.coil.MemoryCacheManagerImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindAppRateManager(impl: AppRateManagerImpl): AppRateManager

    @Binds
    @Singleton
    abstract fun bindAppVersionManager(impl: AppVersionManagerImpl): AppVersionManager

    @Binds
    @Singleton
    abstract fun bindStringResourceProvider(impl: StringResourceProviderImpl): StringResourceProvider

    @Binds
    @Singleton
    abstract fun bindMemoryCacheManager(impl: MemoryCacheManagerImpl): MemoryCacheManager
}
