package fr.twentynine.keepon.di

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.twentynine.keepon.util.StringResourceProvider

@EntryPoint
@InstallIn(SingletonComponent::class)
interface TimeoutIconDataFetcherEntryPoint {
    fun stringResourceProvider(): StringResourceProvider
}
