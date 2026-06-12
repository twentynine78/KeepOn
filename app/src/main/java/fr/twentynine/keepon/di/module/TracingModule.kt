package fr.twentynine.keepon.di.module

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.twentynine.keepon.BuildConfig
import fr.twentynine.keepon.core.util.LogcatDebugTracer
import fr.twentynine.keepon.core.util.NoOpDebugTracer
import fr.twentynine.keepon.domain.gateway.DebugTracer
import javax.inject.Singleton

/**
 * Provides the [DebugTracer]: logcat in debug, no-op in release. `BuildConfig.DEBUG` is a
 * compile-time constant, so the release branch is the only reachable one there and R8 strips
 * [LogcatDebugTracer] (and every trace message) from the release APK.
 */
@Module
@InstallIn(SingletonComponent::class)
object TracingModule {

    @Provides
    @Singleton
    fun provideDebugTracer(): DebugTracer =
        if (BuildConfig.DEBUG) LogcatDebugTracer() else NoOpDebugTracer()
}
