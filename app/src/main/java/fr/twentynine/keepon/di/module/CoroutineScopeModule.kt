package fr.twentynine.keepon.di.module

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.twentynine.keepon.di.qualifier.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CoroutineScopeModule {

    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope =
        // Default (CPU) dispatcher for orchestration; blocking I/O is claimed explicitly
        // via withContext(IO) in the data layer, keeping the IO pool for real I/O.
        CoroutineScope(SupervisorJob() + Dispatchers.Default)
}
