package fr.twentynine.keepon.di

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.twentynine.keepon.data.repo.UserPreferencesRepository

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetRepositoryEntryPoint {
    fun userPreferencesRepository(): UserPreferencesRepository
}
