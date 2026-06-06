package fr.twentynine.keepon.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.twentynine.keepon.domain.gateway.AppRateManager
import fr.twentynine.keepon.core.rating.AppRateManagerImpl
import fr.twentynine.keepon.domain.gateway.NotificationChannelManager
import fr.twentynine.keepon.core.permission.NotificationChannelManagerImpl
import fr.twentynine.keepon.domain.gateway.StringResourceProvider
import fr.twentynine.keepon.core.util.StringResourceProviderImpl
import fr.twentynine.keepon.domain.gateway.MemoryCacheManager
import fr.twentynine.keepon.core.coil.MemoryCacheManagerImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface AppModule {

    @Binds
    @Singleton
    fun bindAppRateManager(impl: AppRateManagerImpl): AppRateManager

    @Binds
    @Singleton
    fun bindNotificationChannelManager(impl: NotificationChannelManagerImpl): NotificationChannelManager

    @Binds
    @Singleton
    fun bindStringResourceProvider(impl: StringResourceProviderImpl): StringResourceProvider

    @Binds
    @Singleton
    fun bindMemoryCacheManager(impl: MemoryCacheManagerImpl): MemoryCacheManager
}
