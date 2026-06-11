package fr.twentynine.keepon.data.repository

import fr.twentynine.keepon.data.enums.DataStoreSourceType
import fr.twentynine.keepon.data.local.PreferenceDataStoreConstants.APP_LAUNCH_COUNT
import fr.twentynine.keepon.data.local.PreferenceDataStoreConstants.IS_FIRST_LAUNCH
import fr.twentynine.keepon.data.local.PreferenceDataStoreConstants.LAST_RUN_VERSION_CODE
import fr.twentynine.keepon.data.local.PreferenceDataStoreHelper
import fr.twentynine.keepon.domain.repository.AppPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

/**
 * DataStore-backed [AppPreferencesRepository]: reads/writes the app-level flags (first launch, launch
 * count, last-run version). Plain persistence only — the legacy "skipIntro" fallback is layered on by
 * [MigratingAppPreferencesRepository], not here.
 */
class AppPreferencesRepositoryImpl @Inject constructor(
    private val preferenceDataStoreHelper: PreferenceDataStoreHelper,
) : AppPreferencesRepository {

    /** Pure read: defaults to true when unset. Legacy migration is applied by the decorator. */
    override fun getIsFirstLaunchFlow(): Flow<Boolean> =
        preferenceDataStoreHelper.getPreference(
            IS_FIRST_LAUNCH,
            true,
            DataStoreSourceType.DATA_SOURCE_BACKED_UP
        )
            .distinctUntilChanged()

    override suspend fun setIsFirstLaunch(isFirstLaunch: Boolean) =
        preferenceDataStoreHelper.putPreference(
            IS_FIRST_LAUNCH,
            isFirstLaunch,
            DataStoreSourceType.DATA_SOURCE_BACKED_UP
        )

    override fun getAppLaunchCountFlow(): Flow<Long> =
        preferenceDataStoreHelper.getPreference(
            APP_LAUNCH_COUNT,
            0L,
            DataStoreSourceType.DATA_SOURCE_BACKED_UP
        )

    override suspend fun getAppLaunchCount(): Long =
        preferenceDataStoreHelper.getLastPreference(
            APP_LAUNCH_COUNT,
            0L,
            DataStoreSourceType.DATA_SOURCE_BACKED_UP
        )

    override suspend fun setAppLaunchCount(appLaunchCount: Long) =
        preferenceDataStoreHelper.putPreference(
            APP_LAUNCH_COUNT,
            appLaunchCount,
            DataStoreSourceType.DATA_SOURCE_BACKED_UP
        )

    override suspend fun getLastRunVersionCode(): Long =
        preferenceDataStoreHelper.getLastPreference(
            LAST_RUN_VERSION_CODE,
            0L,
            DataStoreSourceType.DATA_SOURCE
        )

    override suspend fun setLastRunVersionCode(versionCode: Long) =
        preferenceDataStoreHelper.putPreference(
            LAST_RUN_VERSION_CODE,
            versionCode,
            DataStoreSourceType.DATA_SOURCE
        )
}
