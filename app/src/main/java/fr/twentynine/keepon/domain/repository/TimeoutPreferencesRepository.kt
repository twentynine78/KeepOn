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
    fun getDefaultScreenTimeoutFlow(): Flow<ScreenTimeout>
    suspend fun getDefaultScreenTimeout(): ScreenTimeout
    suspend fun setDefaultScreenTimeout(timeout: ScreenTimeout)
    fun getCurrentScreenTimeoutFlow(): Flow<ScreenTimeout>
    suspend fun getCurrentScreenTimeout(): ScreenTimeout
    suspend fun setCurrentScreenTimeout(timeout: ScreenTimeout)
    fun getPreviousScreenTimeoutFlow(): Flow<ScreenTimeout>
    suspend fun getPreviousScreenTimeout(): ScreenTimeout
    suspend fun setPreviousScreenTimeout(timeout: ScreenTimeout)
    fun getSelectedScreenTimeoutFlow(): Flow<List<ScreenTimeout>>
    suspend fun getSelectedScreenTimeouts(): List<ScreenTimeout>
    suspend fun setSelectedScreenTimeouts(selectedTimeouts: List<ScreenTimeout>)
    fun getResetTimeoutWhenScreenOffFlow(): Flow<Boolean>
    suspend fun getResetTimeoutWhenScreenOff(): Boolean
    suspend fun setResetTimeoutWhenScreenOff(resetWhenScreenOff: Boolean)
}
