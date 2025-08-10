package fr.twentynine.keepon.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import fr.twentynine.keepon.util.AddTileServiceManager
import fr.twentynine.keepon.util.AddTileServiceManagerImpl

@Module
@InstallIn(SingletonComponent::class)
object AddTileServiceManagerModule {

    @Provides
    fun provideAddTileServiceManager(@ApplicationContext context: Context): AddTileServiceManager {
        return AddTileServiceManagerImpl(context)
    }
}
