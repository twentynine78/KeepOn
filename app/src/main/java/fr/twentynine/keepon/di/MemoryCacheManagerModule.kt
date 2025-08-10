package fr.twentynine.keepon.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import fr.twentynine.keepon.util.coil.MemoryCacheManager
import fr.twentynine.keepon.util.coil.MemoryCacheManagerImpl

@Module
@InstallIn(SingletonComponent::class)
object MemoryCacheManagerModule {

    @Provides
    fun provideMemoryCacheManager(@ApplicationContext context: Context): MemoryCacheManager {
        return MemoryCacheManagerImpl(context)
    }
}
