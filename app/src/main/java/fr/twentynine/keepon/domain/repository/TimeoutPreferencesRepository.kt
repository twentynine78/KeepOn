package fr.twentynine.keepon.domain.repository

import fr.twentynine.keepon.domain.model.ScreenTimeout
import kotlinx.coroutines.flow.Flow

/**
 * Pure persistence of screen-timeout values (current / previous / selected list).
 *
 * No business logic: validation against device policy, lazy migration of legacy
 * keys and foreground-service control remain in callers (use cases / façade).
 */
interface TimeoutPreferencesRepository {
    suspend fun getCurrentScreenTimeoutFlow(): Flow<ScreenTimeout>
    suspend fun getCurrentScreenTimeout(): ScreenTimeout
    suspend fun getPreviousScreenTimeoutFlow(): Flow<ScreenTimeout>
    suspend fun getPreviousScreenTimeout(): ScreenTimeout
    suspend fun setPreviousScreenTimeout(timeout: ScreenTimeout)
    suspend fun setSelectedScreenTimeouts(selectedTimeouts: List<ScreenTimeout>)
}
