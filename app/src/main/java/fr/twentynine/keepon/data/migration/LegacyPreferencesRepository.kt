package fr.twentynine.keepon.data.migration

/**
 * Raw access to the deprecated ("Old*") preference keys.
 *
 * Kept in the data/migration package (not domain) because it deals with legacy
 * persistence formats (e.g. [OldTimeoutIconStyle]); it is a transitional concern
 * consumed only by the migration logic.
 */
interface LegacyPreferencesRepository {
    suspend fun getOldResetTimeoutWhenScreenOff(): Boolean?
    suspend fun removeOldResetTimeoutWhenScreenOff()
    suspend fun getOldTimeoutIconStyle(): OldTimeoutIconStyle?
    suspend fun removeOldTimeoutIconStyle()
    suspend fun getOldSelectedScreenTimeouts(): String
    suspend fun removeOldSelectedScreenTimeouts()
    suspend fun getOldAppReviewAsked(): Boolean
    suspend fun removeOldAppReviewAsked()
    suspend fun getOldSkipIntro(): Boolean
    suspend fun removeOldSkipIntro()
}
