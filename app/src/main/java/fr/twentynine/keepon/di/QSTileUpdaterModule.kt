package fr.twentynine.keepon.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import fr.twentynine.keepon.util.QSTileUpdater
import fr.twentynine.keepon.util.QSTileUpdaterImpl

@Module
@InstallIn(SingletonComponent::class)
object QSTileUpdaterModule {

    @Provides
    fun provideQSTileUpdater(@ApplicationContext context: Context): QSTileUpdater {
        return QSTileUpdaterImpl(context)
    }
}
