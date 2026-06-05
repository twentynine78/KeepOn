package fr.twentynine.keepon.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.twentynine.keepon.util.StringResourceProvider
import fr.twentynine.keepon.util.StringResourceProviderImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class StringResourceProviderModule {

    @Binds
    @Singleton
    abstract fun bindStringResourceProvider(impl: StringResourceProviderImpl): StringResourceProvider
}
