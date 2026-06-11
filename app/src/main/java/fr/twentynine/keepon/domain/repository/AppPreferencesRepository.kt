package fr.twentynine.keepon.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Persistence of app-lifecycle preferences (launch count, last run version,
 * first-launch flag). Pure data access — no business logic.
 */
interface AppPreferencesRepository {
    fun getIsFirstLaunchFlow(): Flow<Boolean>
    suspend fun setIsFirstLaunch(isFirstLaunch: Boolean)
    fun getAppLaunchCountFlow(): Flow<Long>
    suspend fun getAppLaunchCount(): Long
    suspend fun setAppLaunchCount(appLaunchCount: Long)
    suspend fun getLastRunVersionCode(): Long
    suspend fun setLastRunVersionCode(versionCode: Long)
}
