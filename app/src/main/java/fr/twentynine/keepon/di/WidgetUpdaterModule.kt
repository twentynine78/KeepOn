package fr.twentynine.keepon.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import fr.twentynine.keepon.util.WidgetUpdater
import fr.twentynine.keepon.util.WidgetUpdaterImpl

@Module
@InstallIn(SingletonComponent::class)
object WidgetUpdaterModule {

    @Provides
    fun provideQSTileUpdater(@ApplicationContext context: Context): WidgetUpdater {
        return WidgetUpdaterImpl(context)
    }
}
