package fr.twentynine.keepon.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.twentynine.keepon.util.DevicePolicyManagerHelper
import fr.twentynine.keepon.util.DevicePolicyManagerHelperImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DevicePolicyManagerHelperModule {

    @Binds
    @Singleton
    abstract fun bindDevicePolicyManagerHelper(impl: DevicePolicyManagerHelperImpl): DevicePolicyManagerHelper
}
