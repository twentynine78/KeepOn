package fr.twentynine.keepon.di.entrypoint

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.twentynine.keepon.domain.gateway.StringResourceProvider

/** Hilt entry point exposing the string provider to non-injected call sites (e.g. the Coil loader setup). */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface StringResourceProviderEntryPoint {
    fun stringResourceProvider(): StringResourceProvider
}
