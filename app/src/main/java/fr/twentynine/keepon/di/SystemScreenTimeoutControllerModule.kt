package fr.twentynine.keepon.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.twentynine.keepon.util.SystemScreenTimeoutController
import fr.twentynine.keepon.util.SystemScreenTimeoutControllerImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SystemScreenTimeoutControllerModule {

    @Binds
    @Singleton
    abstract fun bindSystemScreenTimeoutController(impl: SystemScreenTimeoutControllerImpl): SystemScreenTimeoutController
}
