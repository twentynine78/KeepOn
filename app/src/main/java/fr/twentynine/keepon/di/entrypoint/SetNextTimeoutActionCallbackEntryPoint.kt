package fr.twentynine.keepon.di.entrypoint

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.twentynine.keepon.domain.gateway.DebugTracer
import fr.twentynine.keepon.domain.usecase.timeout.SetNextSystemScreenTimeoutUseCase
import fr.twentynine.keepon.domain.usecase.timeout.ShouldRouteToAppUseCase

/** Hilt entry point giving the widget's Glance action callback access to the timeout-cycling use cases. */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface SetNextTimeoutActionCallbackEntryPoint {
    fun setNextSystemScreenTimeoutUseCase(): SetNextSystemScreenTimeoutUseCase
    fun shouldRouteToAppUseCase(): ShouldRouteToAppUseCase
    fun debugTracer(): DebugTracer
}
