package fr.twentynine.keepon.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.twentynine.keepon.services.ScreenOffReceiverServiceManager
import fr.twentynine.keepon.services.ScreenOffReceiverServiceManagerImpl
import fr.twentynine.keepon.util.DevicePolicyManagerHelper
import fr.twentynine.keepon.util.DevicePolicyManagerHelperImpl
import fr.twentynine.keepon.util.SystemScreenTimeoutController
import fr.twentynine.keepon.util.SystemScreenTimeoutControllerImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SystemModule {

    @Binds
    @Singleton
    abstract fun bindSystemScreenTimeoutController(impl: SystemScreenTimeoutControllerImpl): SystemScreenTimeoutController

    @Binds
    @Singleton
    abstract fun bindDevicePolicyManagerHelper(impl: DevicePolicyManagerHelperImpl): DevicePolicyManagerHelper

    @Binds
    @Singleton
    abstract fun bindScreenOffReceiverServiceManager(impl: ScreenOffReceiverServiceManagerImpl): ScreenOffReceiverServiceManager
}
