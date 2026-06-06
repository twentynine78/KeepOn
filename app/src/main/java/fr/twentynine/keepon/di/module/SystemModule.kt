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
import javax.inject.Singleton

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
}
