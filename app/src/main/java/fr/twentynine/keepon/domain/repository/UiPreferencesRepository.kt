package fr.twentynine.keepon.domain.repository

import fr.twentynine.keepon.domain.model.TimeoutIconStyle
import kotlinx.coroutines.flow.Flow

/**
 * Persistence of UI-related preferences (icon style, QS tile added flag).
 * Pure data access — no business logic.
 */
interface UiPreferencesRepository {
    suspend fun setTimeoutIconStyle(timeoutIconStyle: TimeoutIconStyle)
    suspend fun getQSTileAddedFlow(): Flow<Boolean>
    suspend fun setQSTileAdded(isAdded: Boolean)
}
