package fr.twentynine.keepon.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import fr.twentynine.keepon.util.StringResourceProvider
import fr.twentynine.keepon.util.StringResourceProviderImpl

@Module
@InstallIn(SingletonComponent::class)
object StringResourceProviderModule {

    @Provides
    fun provideStringResourceProvider(@ApplicationContext context: Context): StringResourceProvider {
        return StringResourceProviderImpl(context)
    }
}
