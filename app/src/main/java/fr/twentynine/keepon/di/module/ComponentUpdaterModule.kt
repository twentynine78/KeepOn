package fr.twentynine.keepon.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.twentynine.keepon.domain.gateway.AddTileServiceManager
import fr.twentynine.keepon.core.component.AddTileServiceManagerImpl
import fr.twentynine.keepon.domain.gateway.AppComponentsUpdater
import fr.twentynine.keepon.core.component.AppComponentsUpdaterImpl
import fr.twentynine.keepon.domain.gateway.WidgetUpdater
import fr.twentynine.keepon.ui.widget.WidgetUpdaterImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface ComponentUpdaterModule {

    @Binds
    @Singleton
    fun bindAppComponentsUpdater(impl: AppComponentsUpdaterImpl): AppComponentsUpdater

    @Binds
    @Singleton
    fun bindWidgetUpdater(impl: WidgetUpdaterImpl): WidgetUpdater

    @Binds
    @Singleton
    fun bindAddTileServiceManager(impl: AddTileServiceManagerImpl): AddTileServiceManager
}
