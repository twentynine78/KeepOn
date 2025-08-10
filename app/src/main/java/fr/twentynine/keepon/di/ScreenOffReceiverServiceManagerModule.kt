package fr.twentynine.keepon.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import fr.twentynine.keepon.services.ScreenOffReceiverServiceManager
import fr.twentynine.keepon.services.ScreenOffReceiverServiceManagerImpl

@Module
@InstallIn(SingletonComponent::class)
object ScreenOffReceiverServiceManagerModule {

    @Provides
    fun provideScreenOffReceiverServiceManager(@ApplicationContext context: Context): ScreenOffReceiverServiceManager {
        return ScreenOffReceiverServiceManagerImpl(context)
    }
}
