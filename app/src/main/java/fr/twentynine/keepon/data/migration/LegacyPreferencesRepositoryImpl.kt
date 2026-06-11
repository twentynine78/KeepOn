package fr.twentynine.keepon.data.migration

import fr.twentynine.keepon.data.enums.DataStoreSourceType
import fr.twentynine.keepon.data.local.PreferenceDataStoreConstants.OLD_APP_REVIEW_ASKED
import fr.twentynine.keepon.data.local.PreferenceDataStoreConstants.OLD_RESET_TIMEOUT_WHEN_SCREEN_OFF
import fr.twentynine.keepon.data.local.PreferenceDataStoreConstants.OLD_SELECTED_SCREEN_TIMEOUT
import fr.twentynine.keepon.data.local.PreferenceDataStoreConstants.OLD_SKIP_INTRO
import fr.twentynine.keepon.data.local.PreferenceDataStoreConstants.OLD_TIMEOUT_ICON_STYLE
import fr.twentynine.keepon.data.local.PreferenceDataStoreHelper
import fr.twentynine.keepon.data.local.PreferencesJson
import javax.inject.Inject

/**
 * DataStore-backed [LegacyPreferencesRepository]: reads and clears the legacy `OLD_*` preference keys.
 * Each `get` decodes the old stored format (returning null/empty when absent or unreadable); the
 * paired `remove` lets the migrating repositories drop a key once its value has been carried over.
 */
class LegacyPreferencesRepositoryImpl @Inject constructor(
    private val preferenceDataStoreHelper: PreferenceDataStoreHelper,
) : LegacyPreferencesRepository {

    override suspend fun getOldResetTimeoutWhenScreenOff(): Boolean? =
        preferenceDataStoreHelper.getLastPreference(
            OLD_RESET_TIMEOUT_WHEN_SCREEN_OFF,
            DataStoreSourceType.DATA_SOURCE_BACKED_UP
        )

    override suspend fun removeOldResetTimeoutWhenScreenOff() =
        preferenceDataStoreHelper.removePreference(
            OLD_RESET_TIMEOUT_WHEN_SCREEN_OFF,
            DataStoreSourceType.DATA_SOURCE_BACKED_UP
        )

    override suspend fun getOldTimeoutIconStyle(): OldTimeoutIconStyle? {
        val oldValue = preferenceDataStoreHelper.getLastPreference(
            OLD_TIMEOUT_ICON_STYLE,
            "",
            DataStoreSourceType.DATA_SOURCE_BACKED_UP
        )

        return if (oldValue.isNotEmpty()) {
            // A legacy value that no longer decodes is dropped rather than blocking the migration.
            runCatching { PreferencesJson.decodeFromString<OldTimeoutIconStyle>(oldValue) }.getOrNull()
        } else {
            null
        }
    }

    override suspend fun removeOldTimeoutIconStyle() =
        preferenceDataStoreHelper.removePreference(
            OLD_TIMEOUT_ICON_STYLE,
            DataStoreSourceType.DATA_SOURCE_BACKED_UP
        )

    override suspend fun getOldSelectedScreenTimeouts(): String =
        preferenceDataStoreHelper.getLastPreference(
            OLD_SELECTED_SCREEN_TIMEOUT,
            "",
            DataStoreSourceType.DATA_SOURCE_BACKED_UP
        )

    override suspend fun removeOldSelectedScreenTimeouts() =
        preferenceDataStoreHelper.removePreference(
            OLD_SELECTED_SCREEN_TIMEOUT,
            DataStoreSourceType.DATA_SOURCE_BACKED_UP
        )

    override suspend fun getOldAppReviewAsked(): Boolean =
        preferenceDataStoreHelper.getLastPreference(
            OLD_APP_REVIEW_ASKED,
            false,
            DataStoreSourceType.DATA_SOURCE_BACKED_UP
        )

    override suspend fun removeOldAppReviewAsked() =
        preferenceDataStoreHelper.removePreference(
            OLD_APP_REVIEW_ASKED,
            DataStoreSourceType.DATA_SOURCE_BACKED_UP
        )

    override suspend fun getOldSkipIntro(): Boolean =
        preferenceDataStoreHelper.getLastPreference(
            OLD_SKIP_INTRO,
            false,
            DataStoreSourceType.DATA_SOURCE_BACKED_UP
        )

    override suspend fun removeOldSkipIntro() =
        preferenceDataStoreHelper.removePreference(
            OLD_SKIP_INTRO,
            DataStoreSourceType.DATA_SOURCE_BACKED_UP
        )
}
