package fr.twentynine.keepon.data.repository

import fr.twentynine.keepon.data.enums.DataStoreSourceType
import fr.twentynine.keepon.data.local.PreferenceDataStoreConstants.IS_FIRST_LAUNCH
import fr.twentynine.keepon.data.local.PreferenceDataStoreHelper
import fr.twentynine.keepon.data.migration.LegacyPreferencesRepository
import fr.twentynine.keepon.domain.gateway.DebugTracer
import fr.twentynine.keepon.domain.repository.AppPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Migrating decorator over [AppPreferencesRepositoryImpl].
 *
 * Lazily migrates the deprecated "skipIntro" flag into the current isFirstLaunch
 * preference on first collection of its flow, keeping migration concerns out of the
 * pure repository implementation. The migration is serialized by a mutex so concurrent
 * first reads cannot interleave the check-then-act.
 */
class MigratingAppPreferencesRepository @Inject constructor(
    private val delegate: AppPreferencesRepositoryImpl,
    private val legacyPreferencesRepository: LegacyPreferencesRepository,
    private val preferenceDataStoreHelper: PreferenceDataStoreHelper,
    private val tracer: DebugTracer,
) : AppPreferencesRepository {

    private val ioDispatcher = Dispatchers.IO

    // Serializes the lazy migration: without it a write landing between the
    // "current is unset" check and the migration write could be clobbered.
    private val migrationMutex = Mutex()

    // Fast path: once the migration has been checked once, later collections skip the
    // DataStore read entirely. Safe because the repository is bound as a @Singleton.
    @Volatile private var isFirstLaunchMigrated = false

    override fun getIsFirstLaunchFlow(): Flow<Boolean> = flow {
        migrateIsFirstLaunchIfNeeded()
        emitAll(delegate.getIsFirstLaunchFlow())
    }

    private suspend fun migrateIsFirstLaunchIfNeeded() {
        if (isFirstLaunchMigrated) return
        withContext(ioDispatcher) {
            migrationMutex.withLock {
                if (isFirstLaunchMigrated) return@withLock
                val isFirstLaunch = preferenceDataStoreHelper.getLastPreference(
                    IS_FIRST_LAUNCH,
                    DataStoreSourceType.DATA_SOURCE_BACKED_UP
                )
                // Migrate from the deprecated "skipIntro" flag, once, when unset.
                if (isFirstLaunch == null && legacyPreferencesRepository.getOldSkipIntro()) {
                    tracer.trace("Migration") { "migrating legacy skipIntro flag into isFirstLaunch=false" }
                    delegate.setIsFirstLaunch(false)
                    legacyPreferencesRepository.removeOldSkipIntro()
                }
                isFirstLaunchMigrated = true
            }
        }
    }

    override suspend fun setIsFirstLaunch(isFirstLaunch: Boolean) =
        delegate.setIsFirstLaunch(isFirstLaunch)

    override fun getAppLaunchCountFlow(): Flow<Long> =
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
