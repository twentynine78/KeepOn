package fr.twentynine.keepon.domain.gateway

import kotlinx.coroutines.Job

/**
 * Domain port to refresh the app's external surfaces (QS tile + widget)
 * after a state change. Use cases call this once the data is persisted.
 */
interface AppComponentsUpdater {
    suspend fun requestUpdate(): Job
}
