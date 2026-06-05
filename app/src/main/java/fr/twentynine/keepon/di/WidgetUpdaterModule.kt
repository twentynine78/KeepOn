package fr.twentynine.keepon.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.twentynine.keepon.util.WidgetUpdater
import fr.twentynine.keepon.util.WidgetUpdaterImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class WidgetUpdaterModule {

    @Binds
    @Singleton
    abstract fun bindWidgetUpdater(impl: WidgetUpdaterImpl): WidgetUpdater
}
