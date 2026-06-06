package fr.twentynine.keepon.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.twentynine.keepon.core.rating.AppRateManager
import fr.twentynine.keepon.core.rating.AppRateManagerImpl
import fr.twentynine.keepon.data.migration.AppVersionManager
import fr.twentynine.keepon.data.migration.AppVersionManagerImpl
import fr.twentynine.keepon.domain.gateway.StringResourceProvider
import fr.twentynine.keepon.util.StringResourceProviderImpl
import fr.twentynine.keepon.util.coil.MemoryCacheManager
import fr.twentynine.keepon.util.coil.MemoryCacheManagerImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface AppModule {

    @Binds
    @Singleton
    fun bindAppRateManager(impl: AppRateManagerImpl): AppRateManager

    @Binds
    @Singleton
    fun bindAppVersionManager(impl: AppVersionManagerImpl): AppVersionManager

    @Binds
    @Singleton
    fun bindStringResourceProvider(impl: StringResourceProviderImpl): StringResourceProvider

    @Binds
    @Singleton
    fun bindMemoryCacheManager(impl: MemoryCacheManagerImpl): MemoryCacheManager
}
