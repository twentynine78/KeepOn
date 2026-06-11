package fr.twentynine.keepon.data.repository

import fr.twentynine.keepon.data.enums.DataStoreSourceType
import fr.twentynine.keepon.data.local.PreferenceDataStoreConstants.RESET_TIMEOUT_WHEN_SCREEN_OFF
import fr.twentynine.keepon.data.local.PreferenceDataStoreConstants.SELECTED_SCREEN_TIMEOUT
import fr.twentynine.keepon.data.local.PreferenceDataStoreHelper
import fr.twentynine.keepon.data.migration.LegacyPreferencesRepository
import fr.twentynine.keepon.domain.model.ScreenTimeout
import fr.twentynine.keepon.domain.repository.TimeoutPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Migrating decorator over [TimeoutPreferencesRepositoryImpl].
 *
 * Lazily migrates the deprecated selected-timeout list and "resetTimeoutOnScreenOff"
 * flag into their current keys on first use (first collection of their flows, or first
 * one-shot read), serialized by a mutex so concurrent first reads cannot interleave the
 * check-then-act. The migrated reset value is written raw (no foreground-service side
 * effect): the service state is reconciled by normal operation / the monitor worker.
 */
class MigratingTimeoutPreferencesRepository @Inject constructor(
    private val delegate: TimeoutPreferencesRepositoryImpl,
    private val legacyPreferencesRepository: LegacyPreferencesRepository,
    private val preferenceDataStoreHelper: PreferenceDataStoreHelper,
) : TimeoutPreferencesRepository {

    private val ioDispatcher = Dispatchers.IO

    // Serializes the lazy migrations: without it a write landing between the
    // "current is unset" check and the migration write could be clobbered.
    private val migrationMutex = Mutex()

    // Fast paths: once a migration has been checked once, later uses skip the DataStore
    // read entirely. Safe because the repository is bound as a @Singleton.
    @Volatile private var selectedTimeoutsMigrated = false

    @Volatile private var resetTimeoutWhenScreenOffMigrated = false

    // ----- Migrating getters -----

    override fun getSelectedScreenTimeoutFlow(): Flow<List<ScreenTimeout>> = flow {
        migrateSelectedScreenTimeoutsIfNeeded()
        emitAll(delegate.getSelectedScreenTimeoutFlow())
    }

    override suspend fun getSelectedScreenTimeouts(): List<ScreenTimeout> {
        migrateSelectedScreenTimeoutsIfNeeded()
        return delegate.getSelectedScreenTimeouts()
    }

    override fun getResetTimeoutWhenScreenOffFlow(): Flow<Boolean> = flow {
        migrateResetTimeoutWhenScreenOffIfNeeded()
        emitAll(delegate.getResetTimeoutWhenScreenOffFlow())
    }

    override suspend fun getResetTimeoutWhenScreenOff(): Boolean {
        migrateResetTimeoutWhenScreenOffIfNeeded()
        return delegate.getResetTimeoutWhenScreenOff()
    }

    private suspend fun migrateSelectedScreenTimeoutsIfNeeded() {
        if (selectedTimeoutsMigrated) return
        withContext(ioDispatcher) {
            migrationMutex.withLock {
                if (selectedTimeoutsMigrated) return@withLock
                val current = preferenceDataStoreHelper.getLastPreference(
                    SELECTED_SCREEN_TIMEOUT,
                    DataStoreSourceType.DATA_SOURCE_BACKED_UP
                )
                if (current.isNullOrEmpty()) {
                    val old = legacyPreferencesRepository.getOldSelectedScreenTimeouts()
                    if (old.isNotEmpty()) {
                        val migrated = intListFromStr(old).map { ScreenTimeout(it) }
                        delegate.setSelectedScreenTimeouts(migrated)
                        legacyPreferencesRepository.removeOldSelectedScreenTimeouts()
                    }
                }
                selectedTimeoutsMigrated = true
            }
        }
    }

    private suspend fun migrateResetTimeoutWhenScreenOffIfNeeded() {
        if (resetTimeoutWhenScreenOffMigrated) return
        withContext(ioDispatcher) {
            migrationMutex.withLock {
                if (resetTimeoutWhenScreenOffMigrated) return@withLock
                val current = preferenceDataStoreHelper.getLastPreference(
                    RESET_TIMEOUT_WHEN_SCREEN_OFF,
                    DataStoreSourceType.DATA_SOURCE_BACKED_UP
                )
                if (current == null) {
                    val old = legacyPreferencesRepository.getOldResetTimeoutWhenScreenOff()
                    if (old != null) {
                        // The legacy flag had inverted semantics, hence !old.
                        preferenceDataStoreHelper.putPreference(
                            RESET_TIMEOUT_WHEN_SCREEN_OFF,
                            !old,
                            DataStoreSourceType.DATA_SOURCE_BACKED_UP
                        )
                        legacyPreferencesRepository.removeOldResetTimeoutWhenScreenOff()
                    }
                }
                resetTimeoutWhenScreenOffMigrated = true
            }
        }
    }

    private fun intListFromStr(stringIntList: String?): List<Int> {
        val resultList = ArrayList<Int>()
        stringIntList?.split("|")?.forEach { value ->
            if (value.isNotEmpty()) resultList.add(value.toInt())
        }
        return resultList
    }

    // ----- Pure delegations -----

    override fun getDefaultScreenTimeoutFlow(): Flow<ScreenTimeout> =
        delegate.getDefaultScreenTimeoutFlow()

    override suspend fun getDefaultScreenTimeout(): ScreenTimeout =
        delegate.getDefaultScreenTimeout()

    override suspend fun setDefaultScreenTimeout(timeout: ScreenTimeout) =
        delegate.setDefaultScreenTimeout(timeout)

    override fun getCurrentScreenTimeoutFlow(): Flow<ScreenTimeout> =
        delegate.getCurrentScreenTimeoutFlow()

    override suspend fun getCurrentScreenTimeout(): ScreenTimeout =
        delegate.getCurrentScreenTimeout()

    override suspend fun setCurrentScreenTimeout(timeout: ScreenTimeout) =
        delegate.setCurrentScreenTimeout(timeout)

    override fun getPreviousScreenTimeoutFlow(): Flow<ScreenTimeout> =
        delegate.getPreviousScreenTimeoutFlow()

    override suspend fun getPreviousScreenTimeout(): ScreenTimeout =
        delegate.getPreviousScreenTimeout()

    override suspend fun setPreviousScreenTimeout(timeout: ScreenTimeout) =
        delegate.setPreviousScreenTimeout(timeout)

    override suspend fun setSelectedScreenTimeouts(selectedTimeouts: List<ScreenTimeout>) =
        delegate.setSelectedScreenTimeouts(selectedTimeouts)

    override suspend fun setResetTimeoutWhenScreenOff(resetWhenScreenOff: Boolean) =
        delegate.setResetTimeoutWhenScreenOff(resetWhenScreenOff)
}
