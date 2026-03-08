package fr.twentynine.keepon.di

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.twentynine.keepon.data.repo.UserPreferencesRepository
import fr.twentynine.keepon.util.AppComponentsUpdater

@EntryPoint
@InstallIn(SingletonComponent::class)
interface SetNextTimeoutActionCallbackEntryPoint {
    fun userPreferencesRepository(): UserPreferencesRepository
    fun appComponentsUpdater(): AppComponentsUpdater
}
