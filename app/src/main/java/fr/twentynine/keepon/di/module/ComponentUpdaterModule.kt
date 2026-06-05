package fr.twentynine.keepon.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.twentynine.keepon.ui.components.AddTileServiceManager
import fr.twentynine.keepon.ui.components.AddTileServiceManagerImpl
import fr.twentynine.keepon.ui.components.AppComponentsUpdater
import fr.twentynine.keepon.ui.components.AppComponentsUpdaterImpl
import fr.twentynine.keepon.ui.components.QSTileUpdater
import fr.twentynine.keepon.ui.components.QSTileUpdaterImpl
import fr.twentynine.keepon.ui.components.WidgetUpdater
import fr.twentynine.keepon.ui.components.WidgetUpdaterImpl
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
