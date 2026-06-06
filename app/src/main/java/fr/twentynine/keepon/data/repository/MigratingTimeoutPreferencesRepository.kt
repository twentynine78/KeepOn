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
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Migrating decorator over [TimeoutPreferencesRepositoryImpl].
 *
 * Lazily migrates the deprecated selected-timeout list and "resetTimeoutOnScreenOff"
 * flag into their current keys on first read. Migration stays lazy (atomic per read) —
 * no startup race. The migrated reset value is written raw (no foreground-service
 * side effect): the service state is reconciled by normal operation / the monitor worker.
 */
class MigratingTimeoutPreferencesRepository @Inject constructor(
    private val delegate: TimeoutPreferencesRepositoryImpl,
    private val legacyPreferencesRepository: LegacyPreferencesRepository,
    private val preferenceDataStoreHelper: PreferenceDataStoreHelper,
) : TimeoutPreferencesRepository {

    private val ioDispatcher = Dispatchers.IO

    // ----- Migrating getters -----

    override suspend fun getSelectedScreenTimeoutFlow(): Flow<List<ScreenTimeout>> {
        migrateSelectedScreenTimeoutsIfNeeded()
        return delegate.getSelectedScreenTimeoutFlow()
    }

    override suspend fun getSelectedScreenTimeouts(): List<ScreenTimeout> {
        migrateSelectedScreenTimeoutsIfNeeded()
        return delegate.getSelectedScreenTimeouts()
    }

    override suspend fun getResetTimeoutWhenScreenOffFlow(): Flow<Boolean> {
        migrateResetTimeoutWhenScreenOffIfNeeded()
        return delegate.getResetTimeoutWhenScreenOffFlow()
    }

    override suspend fun getResetTimeoutWhenScreenOff(): Boolean {
        migrateResetTimeoutWhenScreenOffIfNeeded()
        return delegate.getResetTimeoutWhenScreenOff()
    }

    private suspend fun migrateSelectedScreenTimeoutsIfNeeded() =
        withContext(ioDispatcher) {
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
        }

    private suspend fun migrateResetTimeoutWhenScreenOffIfNeeded() =
        withContext(ioDispatcher) {
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
        }

    private fun intListFromStr(stringIntList: String?): List<Int> {
        val resultList = ArrayList<Int>()
        stringIntList?.split("|")?.forEach { value ->
            if (value.isNotEmpty()) resultList.add(value.toInt())
        }
        return resultList
    }

    // ----- Pure delegations -----

    override suspend fun getDefaultScreenTimeoutFlow(): Flow<ScreenTimeout> =
        delegate.getDefaultScreenTimeoutFlow()

    override suspend fun getDefaultScreenTimeout(): ScreenTimeout =
        delegate.getDefaultScreenTimeout()

    override suspend fun setDefaultScreenTimeout(timeout: ScreenTimeout) =
        delegate.setDefaultScreenTimeout(timeout)

    override suspend fun getCurrentScreenTimeoutFlow(): Flow<ScreenTimeout> =
        delegate.getCurrentScreenTimeoutFlow()

    override suspend fun getCurrentScreenTimeout(): ScreenTimeout =
        delegate.getCurrentScreenTimeout()

    override suspend fun setCurrentScreenTimeout(timeout: ScreenTimeout) =
        delegate.setCurrentScreenTimeout(timeout)

    override suspend fun getPreviousScreenTimeoutFlow(): Flow<ScreenTimeout> =
        delegate.getPreviousScreenTimeoutFlow()

    override suspend fun getPreviousScreenTimeout(): ScreenTimeout =
        delegate.getPreviousScreenTimeout()

    override suspend fun setPreviousScreenTimeout(timeout: ScreenTimeout) =
        delegate.setPreviousScreenTimeout(timeout)

    override suspend fun setSelectedScreenTimeouts(selectedTimeouts: List<ScreenTimeout>) =
        delegate.setSelectedScreenTimeouts(selectedTimeouts)
}
