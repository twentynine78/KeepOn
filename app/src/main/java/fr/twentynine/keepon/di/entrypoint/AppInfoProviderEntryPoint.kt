package fr.twentynine.keepon.di.entrypoint

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.twentynine.keepon.domain.gateway.AppInfoProvider

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AppInfoProviderEntryPoint {
    fun appInfoProvider(): AppInfoProvider
}
