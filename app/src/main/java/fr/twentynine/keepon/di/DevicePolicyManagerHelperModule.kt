package fr.twentynine.keepon.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import fr.twentynine.keepon.util.DevicePolicyManagerHelper
import fr.twentynine.keepon.util.DevicePolicyManagerHelperImpl

@Module
@InstallIn(SingletonComponent::class)
object DevicePolicyManagerHelperModule {

    @Provides
    fun provideDevicePolicyManagerHelper(@ApplicationContext context: Context): DevicePolicyManagerHelper {
        return DevicePolicyManagerHelperImpl(context)
    }
}
