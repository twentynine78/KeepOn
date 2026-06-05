package fr.twentynine.keepon.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.twentynine.keepon.services.ScreenOffReceiverServiceManager
import fr.twentynine.keepon.services.ScreenOffReceiverServiceManagerImpl
import fr.twentynine.keepon.util.system.DevicePolicyController
import fr.twentynine.keepon.util.system.DevicePolicyControllerImpl
import fr.twentynine.keepon.util.timeout.SystemScreenTimeoutController
import fr.twentynine.keepon.util.timeout.SystemScreenTimeoutControllerImpl
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
