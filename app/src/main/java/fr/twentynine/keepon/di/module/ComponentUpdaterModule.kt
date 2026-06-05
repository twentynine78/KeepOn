package fr.twentynine.keepon.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.twentynine.keepon.util.AddTileServiceManager
import fr.twentynine.keepon.util.AddTileServiceManagerImpl
import fr.twentynine.keepon.util.AppComponentsUpdater
import fr.twentynine.keepon.util.AppComponentsUpdaterImpl
import fr.twentynine.keepon.util.QSTileUpdater
import fr.twentynine.keepon.util.QSTileUpdaterImpl
import fr.twentynine.keepon.util.WidgetUpdater
import fr.twentynine.keepon.util.WidgetUpdaterImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ComponentUpdaterModule {

    @Binds
    @Singleton
    abstract fun bindAppComponentsUpdater(impl: AppComponentsUpdaterImpl): AppComponentsUpdater

    @Binds
    @Singleton
    abstract fun bindQSTileUpdater(impl: QSTileUpdaterImpl): QSTileUpdater

    @Binds
    @Singleton
    abstract fun bindWidgetUpdater(impl: WidgetUpdaterImpl): WidgetUpdater

    @Binds
    @Singleton
    abstract fun bindAddTileServiceManager(impl: AddTileServiceManagerImpl): AddTileServiceManager
}
