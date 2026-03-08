package fr.twentynine.keepon.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.twentynine.keepon.util.AppComponentsUpdater
import fr.twentynine.keepon.util.AppComponentsUpdaterImpl
import fr.twentynine.keepon.util.QSTileUpdater
import fr.twentynine.keepon.util.WidgetUpdater

@Module
@InstallIn(SingletonComponent::class)
object AppComponentsUpdaterModule {

    @Provides
    fun provideAppComponentsUpdater(
        qsTileUpdater: QSTileUpdater,
        widgetUpdater: WidgetUpdater,
    ): AppComponentsUpdater {
        return AppComponentsUpdaterImpl(qsTileUpdater, widgetUpdater)
    }
}
