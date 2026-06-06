package fr.twentynine.keepon.data.repository

import fr.twentynine.keepon.data.enums.DataStoreSourceType
import fr.twentynine.keepon.data.local.PreferenceDataStoreConstants.IS_FIRST_LAUNCH
import fr.twentynine.keepon.data.local.PreferenceDataStoreHelper
import fr.twentynine.keepon.data.migration.LegacyPreferencesRepository
import fr.twentynine.keepon.domain.repository.AppPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Migrating decorator over [AppPreferencesRepositoryImpl].
 *
 * Lazily migrates the deprecated "skipIntro" flag into the current isFirstLaunch
 * preference on first read, keeping migration concerns out of the pure repository
 * implementation. Migration stays lazy (atomic per read) — no startup race.
 */
class MigratingAppPreferencesRepository @Inject constructor(
    private val delegate: AppPreferencesRepositoryImpl,
    private val legacyPreferencesRepository: LegacyPreferencesRepository,
    private val preferenceDataStoreHelper: PreferenceDataStoreHelper,
) : AppPreferencesRepository {

    private val ioDispatcher = Dispatchers.IO

    override suspend fun getIsFirstLaunchFlow(): Flow<Boolean> {
        migrateIsFirstLaunchIfNeeded()
        return delegate.getIsFirstLaunchFlow()
    }

    private suspend fun migrateIsFirstLaunchIfNeeded() =
        withContext(ioDispatcher) {
            val isFirstLaunch = preferenceDataStoreHelper.getLastPreference(
                IS_FIRST_LAUNCH,
                DataStoreSourceType.DATA_SOURCE_BACKED_UP
            )
            // Migrate from the deprecated "skipIntro" flag, once, when unset.
            if (isFirstLaunch == null && legacyPreferencesRepository.getOldSkipIntro()) {
                delegate.setIsFirstLaunch(false)
                legacyPreferencesRepository.removeOldSkipIntro()
            }
        }

    override suspend fun setIsFirstLaunch(isFirstLaunch: Boolean) =
        delegate.setIsFirstLaunch(isFirstLaunch)

    override suspend fun getAppLaunchCountFlow(): Flow<Long> =
        delegate.getAppLaunchCountFlow()

    override suspend fun getAppLaunchCount(): Long =
        delegate.getAppLaunchCount()

    override suspend fun setAppLaunchCount(appLaunchCount: Long) =
        delegate.setAppLaunchCount(appLaunchCount)

    override suspend fun getLastRunVersionCode(): Long =
        delegate.getLastRunVersionCode()

    override suspend fun setLastRunVersionCode(versionCode: Long) =
        delegate.setLastRunVersionCode(versionCode)
}
