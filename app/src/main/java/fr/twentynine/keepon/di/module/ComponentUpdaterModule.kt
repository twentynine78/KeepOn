package fr.twentynine.keepon.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.twentynine.keepon.util.component.AddTileServiceManager
import fr.twentynine.keepon.util.component.AddTileServiceManagerImpl
import fr.twentynine.keepon.util.component.AppComponentsUpdater
import fr.twentynine.keepon.util.component.AppComponentsUpdaterImpl
import fr.twentynine.keepon.util.component.QSTileUpdater
import fr.twentynine.keepon.util.component.QSTileUpdaterImpl
import fr.twentynine.keepon.util.component.WidgetUpdater
import fr.twentynine.keepon.util.component.WidgetUpdaterImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface ComponentUpdaterModule {

    @Binds
    @Singleton
    fun bindAppComponentsUpdater(impl: AppComponentsUpdaterImpl): AppComponentsUpdater

    @Binds
    @Singleton
    fun bindQSTileUpdater(impl: QSTileUpdaterImpl): QSTileUpdater

    @Binds
    @Singleton
    fun bindWidgetUpdater(impl: WidgetUpdaterImpl): WidgetUpdater

    @Binds
    @Singleton
    fun bindAddTileServiceManager(impl: AddTileServiceManagerImpl): AddTileServiceManager
}
