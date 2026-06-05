package fr.twentynine.keepon.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.twentynine.keepon.util.QSTileUpdater
import fr.twentynine.keepon.util.QSTileUpdaterImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class QSTileUpdaterModule {

    @Binds
    @Singleton
    abstract fun bindQSTileUpdater(impl: QSTileUpdaterImpl): QSTileUpdater
}
