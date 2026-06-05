package fr.twentynine.keepon.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.twentynine.keepon.util.AppRateHelper
import fr.twentynine.keepon.util.AppRateHelperImpl
import fr.twentynine.keepon.util.AppVersionManager
import fr.twentynine.keepon.util.AppVersionManagerImpl
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
    abstract fun bindAppRateHelper(impl: AppRateHelperImpl): AppRateHelper

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
