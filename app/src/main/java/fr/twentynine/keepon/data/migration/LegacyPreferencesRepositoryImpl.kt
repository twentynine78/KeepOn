package fr.twentynine.keepon.data.migration

import fr.twentynine.keepon.data.enums.DataStoreSourceType
import fr.twentynine.keepon.data.local.PreferenceDataStoreConstants.OLD_APP_REVIEW_ASKED
import fr.twentynine.keepon.data.local.PreferenceDataStoreConstants.OLD_RESET_TIMEOUT_WHEN_SCREEN_OFF
import fr.twentynine.keepon.data.local.PreferenceDataStoreConstants.OLD_SELECTED_SCREEN_TIMEOUT
import fr.twentynine.keepon.data.local.PreferenceDataStoreConstants.OLD_SKIP_INTRO
import fr.twentynine.keepon.data.local.PreferenceDataStoreConstants.OLD_TIMEOUT_ICON_STYLE
import fr.twentynine.keepon.data.local.PreferenceDataStoreHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject

/**
 * DataStore-backed [LegacyPreferencesRepository]: reads and clears the legacy `OLD_*` preference keys
 * on the IO dispatcher. Each `get` decodes the old stored format (returning null/empty when absent);
 * the paired `remove` lets the migrating repositories drop a key once its value has been carried over.
 */
class LegacyPreferencesRepositoryImpl @Inject constructor(
    private val preferenceDataStoreHelper: PreferenceDataStoreHelper,
) : LegacyPreferencesRepository {

    private val ioDispatcher = Dispatchers.IO

    override suspend fun getOldResetTimeoutWhenScreenOff(): Boolean? =
        withContext(ioDispatcher) {
            preferenceDataStoreHelper.getLastPreference(
                OLD_RESET_TIMEOUT_WHEN_SCREEN_OFF,
                DataStoreSourceType.DATA_SOURCE_BACKED_UP
            )
        }

    override suspend fun removeOldResetTimeoutWhenScreenOff() =
        withContext(ioDispatcher) {
            preferenceDataStoreHelper.removePreference(
                OLD_RESET_TIMEOUT_WHEN_SCREEN_OFF,
                DataStoreSourceType.DATA_SOURCE_BACKED_UP
            )
        }

    override suspend fun getOldTimeoutIconStyle(): OldTimeoutIconStyle? =
        withContext(ioDispatcher) {
            val oldValue = preferenceDataStoreHelper.getLastPreference(
                OLD_TIMEOUT_ICON_STYLE,
                "",
                DataStoreSourceType.DATA_SOURCE_BACKED_UP
            )

            if (oldValue.isNotEmpty()) {
                Json.decodeFromString<OldTimeoutIconStyle>(oldValue)
            } else {
                null
            }
        }

    override suspend fun removeOldTimeoutIconStyle() =
        withContext(ioDispatcher) {
            preferenceDataStoreHelper.removePreference(
                OLD_TIMEOUT_ICON_STYLE,
                DataStoreSourceType.DATA_SOURCE_BACKED_UP
            )
        }

    override suspend fun getOldSelectedScreenTimeouts(): String =
        withContext(ioDispatcher) {
            val defaultValue = ""
            preferenceDataStoreHelper.getLastPreference(
                OLD_SELECTED_SCREEN_TIMEOUT,
                defaultValue,
                DataStoreSourceType.DATA_SOURCE_BACKED_UP
            )
        }

    override suspend fun removeOldSelectedScreenTimeouts() =
        withContext(ioDispatcher) {
            preferenceDataStoreHelper.removePreference(
                OLD_SELECTED_SCREEN_TIMEOUT,
                DataStoreSourceType.DATA_SOURCE_BACKED_UP
            )
        }

    override suspend fun getOldAppReviewAsked(): Boolean =
        withContext(ioDispatcher) {
            val defaultValue = false
            preferenceDataStoreHelper.getLastPreference(
                OLD_APP_REVIEW_ASKED,
                defaultValue,
                DataStoreSourceType.DATA_SOURCE_BACKED_UP
            )
        }

    override suspend fun removeOldAppReviewAsked() =
        withContext(ioDispatcher) {
            preferenceDataStoreHelper.removePreference(
                OLD_APP_REVIEW_ASKED,
                DataStoreSourceType.DATA_SOURCE_BACKED_UP
            )
        }

    override suspend fun getOldSkipIntro(): Boolean =
        withContext(ioDispatcher) {
            val defaultValue = false
            preferenceDataStoreHelper.getLastPreference(
                OLD_SKIP_INTRO,
                defaultValue,
                DataStoreSourceType.DATA_SOURCE_BACKED_UP
            )
        }

    override suspend fun removeOldSkipIntro() =
        withContext(ioDispatcher) {
            preferenceDataStoreHelper.removePreference(
                OLD_SKIP_INTRO,
                DataStoreSourceType.DATA_SOURCE_BACKED_UP
            )
        }
}
