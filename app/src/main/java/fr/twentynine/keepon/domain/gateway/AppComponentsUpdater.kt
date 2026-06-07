package fr.twentynine.keepon.domain.gateway

import kotlinx.coroutines.Job

/**
 * Domain port to refresh the app's external surfaces after a state change.
 * Use cases call this once the data is persisted. The QS tile refreshes itself
 * (it observes the state while visible), so only the widget needs an explicit push.
 */
interface AppComponentsUpdater {
    suspend fun requestUpdate(): Job
}
