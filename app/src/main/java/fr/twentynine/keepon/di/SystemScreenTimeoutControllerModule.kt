package fr.twentynine.keepon.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import fr.twentynine.keepon.util.SystemScreenTimeoutController
import fr.twentynine.keepon.util.SystemScreenTimeoutControllerImpl

@Module
@InstallIn(SingletonComponent::class)
object SystemScreenTimeoutControllerModule {

    @Provides
    fun provideSystemScreenTimeoutController(@ApplicationContext context: Context): SystemScreenTimeoutController {
        return SystemScreenTimeoutControllerImpl(context)
    }
}
