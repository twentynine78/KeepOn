package fr.twentynine.keepon.domain.repository

import fr.twentynine.keepon.domain.model.ScreenTimeout
import kotlinx.coroutines.flow.Flow

/**
 * Persistence of screen-timeout values (default / current / previous / selected list)
 * and the "reset when screen off" flag.
 *
 * The default timeout is self-initializing (from the system value) and device-policy
 * validated; the selected list is device-policy filtered. Legacy migration of the
 * deprecated keys is applied by a decorator, not here.
 */
interface TimeoutPreferencesRepository {
    suspend fun getDefaultScreenTimeoutFlow(): Flow<ScreenTimeout>
    suspend fun getDefaultScreenTimeout(): ScreenTimeout
    suspend fun setDefaultScreenTimeout(timeout: ScreenTimeout)
    suspend fun getCurrentScreenTimeoutFlow(): Flow<ScreenTimeout>
    suspend fun getCurrentScreenTimeout(): ScreenTimeout
    suspend fun setCurrentScreenTimeout(timeout: ScreenTimeout)
    suspend fun getPreviousScreenTimeoutFlow(): Flow<ScreenTimeout>
    suspend fun getPreviousScreenTimeout(): ScreenTimeout
    suspend fun setPreviousScreenTimeout(timeout: ScreenTimeout)
    suspend fun getSelectedScreenTimeoutFlow(): Flow<List<ScreenTimeout>>
    suspend fun getSelectedScreenTimeouts(): List<ScreenTimeout>
    suspend fun setSelectedScreenTimeouts(selectedTimeouts: List<ScreenTimeout>)
    suspend fun getResetTimeoutWhenScreenOffFlow(): Flow<Boolean>
    suspend fun getResetTimeoutWhenScreenOff(): Boolean
    suspend fun setResetTimeoutWhenScreenOff(resetWhenScreenOff: Boolean)
}
