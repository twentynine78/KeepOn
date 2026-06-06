package fr.twentynine.keepon.di.entrypoint

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.twentynine.keepon.domain.usecase.timeout.SetNextSystemScreenTimeoutUseCase

@EntryPoint
@InstallIn(SingletonComponent::class)
interface SetNextTimeoutActionCallbackEntryPoint {
    fun setNextSystemScreenTimeoutUseCase(): SetNextSystemScreenTimeoutUseCase
}
