package fr.twentynine.keepon.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.twentynine.keepon.domain.gateway.ScreenOffReceiverServiceManager
import fr.twentynine.keepon.core.service.ScreenOffReceiverServiceManagerImpl
import fr.twentynine.keepon.domain.gateway.DevicePolicyController
import fr.twentynine.keepon.core.policy.DevicePolicyControllerImpl
import fr.twentynine.keepon.domain.gateway.SystemScreenTimeoutController
import fr.twentynine.keepon.core.system.SystemScreenTimeoutControllerImpl
import fr.twentynine.keepon.domain.gateway.AppVersionProvider
import fr.twentynine.keepon.core.system.AppVersionProviderImpl
import fr.twentynine.keepon.domain.gateway.AppInfoProvider
import fr.twentynine.keepon.core.system.AppInfoProviderImpl
import fr.twentynine.keepon.domain.gateway.DynamicShortcutManager
import fr.twentynine.keepon.core.system.DynamicShortcutManagerImpl
import fr.twentynine.keepon.domain.gateway.ScreenTimeoutScheduler
import fr.twentynine.keepon.core.worker.ScreenTimeoutSchedulerImpl
import fr.twentynine.keepon.domain.gateway.UserNotifier
import fr.twentynine.keepon.core.system.UserNotifierImpl
import javax.inject.Singleton

/** Binds the singleton gateways that wrap Android system services (timeout, policy, version, etc.). */
@Module
@InstallIn(SingletonComponent::class)
interface SystemModule {

    @Binds
    @Singleton
    fun bindSystemScreenTimeoutController(impl: SystemScreenTimeoutControllerImpl): SystemScreenTimeoutController

    @Binds
    @Singleton
    fun bindDevicePolicyController(impl: DevicePolicyControllerImpl): DevicePolicyController

    @Binds
    @Singleton
    fun bindScreenOffReceiverServiceManager(impl: ScreenOffReceiverServiceManagerImpl): ScreenOffReceiverServiceManager

    @Binds
    @Singleton
    fun bindAppVersionProvider(impl: AppVersionProviderImpl): AppVersionProvider

    @Binds
    @Singleton
    fun bindDynamicShortcutManager(impl: DynamicShortcutManagerImpl): DynamicShortcutManager

    @Binds
    @Singleton
    fun bindAppInfoProvider(impl: AppInfoProviderImpl): AppInfoProvider

    @Binds
    @Singleton
    fun bindScreenTimeoutScheduler(impl: ScreenTimeoutSchedulerImpl): ScreenTimeoutScheduler

    @Binds
    @Singleton
    fun bindUserNotifier(impl: UserNotifierImpl): UserNotifier
}
