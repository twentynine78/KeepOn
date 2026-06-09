package fr.twentynine.keepon.data.repository

import fr.twentynine.keepon.data.enums.DataStoreSourceType
import fr.twentynine.keepon.data.local.PreferenceDataStoreConstants.APP_LAUNCH_COUNT
import fr.twentynine.keepon.data.local.PreferenceDataStoreConstants.IS_FIRST_LAUNCH
import fr.twentynine.keepon.data.local.PreferenceDataStoreConstants.LAST_RUN_VERSION_CODE
import fr.twentynine.keepon.data.local.PreferenceDataStoreHelper
import fr.twentynine.keepon.domain.repository.AppPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * DataStore-backed [AppPreferencesRepository]: reads/writes the app-level flags (first launch, launch
 * count, last-run version) on the IO dispatcher. Plain persistence only — the legacy "skipIntro"
 * fallback is layered on by [MigratingAppPreferencesRepository], not here.
 */
class AppPreferencesRepositoryImpl @Inject constructor(
    private val preferenceDataStoreHelper: PreferenceDataStoreHelper,
) : AppPreferencesRepository {

    private val ioDispatcher = Dispatchers.IO

    /** Pure read: defaults to true when unset. Legacy migration is applied by the decorator. */
    override suspend fun getIsFirstLaunchFlow(): Flow<Boolean> =
        withContext(ioDispatcher) {
            preferenceDataStoreHelper.getPreference(
                IS_FIRST_LAUNCH,
                true,
                DataStoreSourceType.DATA_SOURCE_BACKED_UP
            )
                .distinctUntilChanged()
        }

    override suspend fun setIsFirstLaunch(isFirstLaunch: Boolean) =
        withContext(ioDispatcher) {
            preferenceDataStoreHelper.putPreference(
                IS_FIRST_LAUNCH,
                isFirstLaunch,
                DataStoreSourceType.DATA_SOURCE_BACKED_UP
            )
        }

    override suspend fun getAppLaunchCountFlow(): Flow<Long> =
        withContext(ioDispatcher) {
            val defaultValue = 0L
            preferenceDataStoreHelper.getPreference(
                APP_LAUNCH_COUNT,
                defaultValue,
                DataStoreSourceType.DATA_SOURCE_BACKED_UP
            )
        }

    override suspend fun getAppLaunchCount(): Long =
        withContext(ioDispatcher) {
            val defaultValue = 0L
            preferenceDataStoreHelper.getLastPreference(
                APP_LAUNCH_COUNT,
                defaultValue,
                DataStoreSourceType.DATA_SOURCE_BACKED_UP
            )
        }

    override suspend fun setAppLaunchCount(appLaunchCount: Long) =
        withContext(ioDispatcher) {
            preferenceDataStoreHelper.putPreference(
                APP_LAUNCH_COUNT,
                appLaunchCount,
                DataStoreSourceType.DATA_SOURCE_BACKED_UP
            )
        }

    override suspend fun getLastRunVersionCode(): Long {
        return preferenceDataStoreHelper.getLastPreference(
            LAST_RUN_VERSION_CODE,
            0L,
            DataStoreSourceType.DATA_SOURCE
        )
    }

    override suspend fun setLastRunVersionCode(versionCode: Long) =
        withContext(ioDispatcher) {
            preferenceDataStoreHelper.putPreference(
                LAST_RUN_VERSION_CODE,
                versionCode,
                DataStoreSourceType.DATA_SOURCE
            )
        }
}
