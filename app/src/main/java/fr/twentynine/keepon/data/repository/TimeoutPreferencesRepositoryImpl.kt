package fr.twentynine.keepon.data.repository

import fr.twentynine.keepon.data.enums.DataStoreSourceType
import fr.twentynine.keepon.data.local.PreferenceDataStoreConstants.CURRENT_SCREEN_TIMEOUT
import fr.twentynine.keepon.data.local.PreferenceDataStoreConstants.DEFAULT_SCREEN_TIMEOUT
import fr.twentynine.keepon.data.local.PreferenceDataStoreConstants.PREVIOUS_SCREEN_TIMEOUT
import fr.twentynine.keepon.data.local.PreferenceDataStoreConstants.RESET_TIMEOUT_WHEN_SCREEN_OFF
import fr.twentynine.keepon.data.local.PreferenceDataStoreConstants.SELECTED_SCREEN_TIMEOUT
import fr.twentynine.keepon.data.local.PreferenceDataStoreHelper
import fr.twentynine.keepon.data.local.PreferencesJson
import fr.twentynine.keepon.domain.catalog.ScreenTimeoutCatalog
import fr.twentynine.keepon.domain.gateway.DevicePolicyController
import fr.twentynine.keepon.domain.gateway.SystemScreenTimeoutController
import fr.twentynine.keepon.domain.model.ScreenTimeout
import fr.twentynine.keepon.domain.repository.TimeoutPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * DataStore-backed [TimeoutPreferencesRepository]: persists the default / current / previous timeouts
 * and the selected-timeout list. The default self-initializes from the live system value when unset
 * and is validated against the device policy; the selected list seeds itself from the catalog on a
 * fresh install (also the fallback when the stored list no longer decodes). Legacy-key fallbacks are
 * layered on by [MigratingTimeoutPreferencesRepository], not here.
 */
class TimeoutPreferencesRepositoryImpl @Inject constructor(
    private val preferenceDataStoreHelper: PreferenceDataStoreHelper,
    private val systemScreenTimeoutController: SystemScreenTimeoutController,
    private val devicePolicyController: DevicePolicyController,
) : TimeoutPreferencesRepository {

    private val ioDispatcher = Dispatchers.IO

    // ----- Default (self-initializing from system, device-policy validated) -----

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getDefaultScreenTimeoutFlow(): Flow<ScreenTimeout> {
        val defaultValue = -1
        return preferenceDataStoreHelper.getPreference(
            DEFAULT_SCREEN_TIMEOUT,
            defaultValue,
            DataStoreSourceType.DATA_SOURCE
        )
            .transformLatest { storedValue ->
                val currentScreenTimeout = ScreenTimeout(storedValue)

                when {
                    storedValue == defaultValue -> emit(initDefaultScreenTimeout())
                    !devicePolicyController.isValidTimeout(currentScreenTimeout) -> emit(setDefaultToMaxAllowedValue())
                    else -> emit(currentScreenTimeout)
                }
            }
            .distinctUntilChanged()
    }

    override suspend fun getDefaultScreenTimeout(): ScreenTimeout {
        val defaultValue = -1
        val persistedTimeoutValue = preferenceDataStoreHelper.getLastPreference(
            DEFAULT_SCREEN_TIMEOUT,
            defaultValue,
            DataStoreSourceType.DATA_SOURCE
        )

        var defaultScreenTimeout = if (persistedTimeoutValue == defaultValue) {
            initDefaultScreenTimeout()
        } else {
            ScreenTimeout(persistedTimeoutValue)
        }

        if (!devicePolicyController.isValidTimeout(defaultScreenTimeout)) {
            defaultScreenTimeout = setDefaultToMaxAllowedValue()
        }

        return defaultScreenTimeout
    }

    override suspend fun setDefaultScreenTimeout(timeout: ScreenTimeout) =
        preferenceDataStoreHelper.putPreference(
            DEFAULT_SCREEN_TIMEOUT,
            timeout.value,
            DataStoreSourceType.DATA_SOURCE
        )

    private suspend fun initDefaultScreenTimeout(): ScreenTimeout =
        withContext(ioDispatcher) {
            val initialValue = systemScreenTimeoutController.getSystemScreenTimeout()
            setDefaultScreenTimeout(initialValue)
            initialValue
        }

    private suspend fun setDefaultToMaxAllowedValue(): ScreenTimeout =
        withContext(ioDispatcher) {
            val maxAllowedScreenTimeout = devicePolicyController.getMaxAllowedScreenTimeout()
            val suitableTimeout = ScreenTimeoutCatalog.screenTimeouts.lastOrNull { it.value <= maxAllowedScreenTimeout }
                ?: error("No suitable screen timeout found within the allowed maximum.")
            setDefaultScreenTimeout(suitableTimeout)
            suitableTimeout
        }

    // ----- Current -----

    override fun getCurrentScreenTimeoutFlow(): Flow<ScreenTimeout> = flow {
        // Resolved at collection time, so every new collector seeds from a fresh system value.
        val defaultValue = systemScreenTimeoutController.getSystemScreenTimeout()
        emitAll(
            preferenceDataStoreHelper.getPreference(
                CURRENT_SCREEN_TIMEOUT,
                defaultValue.value,
                DataStoreSourceType.DATA_SOURCE
            )
                .map { ScreenTimeout(it) }
        )
    }.distinctUntilChanged()

    override suspend fun getCurrentScreenTimeout(): ScreenTimeout {
        val defaultValue = systemScreenTimeoutController.getSystemScreenTimeout()

        return ScreenTimeout(
            preferenceDataStoreHelper.getLastPreference(
                CURRENT_SCREEN_TIMEOUT,
                defaultValue.value,
                DataStoreSourceType.DATA_SOURCE
            )
        )
    }

    override suspend fun setCurrentScreenTimeout(timeout: ScreenTimeout) =
        preferenceDataStoreHelper.putPreference(
            CURRENT_SCREEN_TIMEOUT,
            timeout.value,
            DataStoreSourceType.DATA_SOURCE
        )

    // ----- Previous -----

    override fun getPreviousScreenTimeoutFlow(): Flow<ScreenTimeout> {
        val defaultValue = -1
        return preferenceDataStoreHelper.getPreference(
            PREVIOUS_SCREEN_TIMEOUT,
            defaultValue,
            DataStoreSourceType.DATA_SOURCE
        )
            .map { ScreenTimeout(it) }
            .distinctUntilChanged()
    }

    override suspend fun getPreviousScreenTimeout(): ScreenTimeout =
        ScreenTimeout(
            preferenceDataStoreHelper.getLastPreference(
                PREVIOUS_SCREEN_TIMEOUT,
                -1,
                DataStoreSourceType.DATA_SOURCE
            )
        )

    override suspend fun setPreviousScreenTimeout(timeout: ScreenTimeout) =
        preferenceDataStoreHelper.putPreference(
            PREVIOUS_SCREEN_TIMEOUT,
            timeout.value,
            DataStoreSourceType.DATA_SOURCE
        )

    // ----- Selected (device-policy filtered) -----

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getSelectedScreenTimeoutFlow(): Flow<List<ScreenTimeout>> =
        preferenceDataStoreHelper.getPreference(
            SELECTED_SCREEN_TIMEOUT,
            DataStoreSourceType.DATA_SOURCE_BACKED_UP
        )
            .transformLatest { strList ->
                emit(devicePolicyController.removeNotAllowedScreenTimeout(decodeSelectedScreenTimeouts(strList)))
            }
            .distinctUntilChanged()

    override suspend fun getSelectedScreenTimeouts(): List<ScreenTimeout> =
        devicePolicyController.removeNotAllowedScreenTimeout(
            decodeSelectedScreenTimeouts(
                preferenceDataStoreHelper.getLastPreference(
                    SELECTED_SCREEN_TIMEOUT,
                    DataStoreSourceType.DATA_SOURCE_BACKED_UP
                )
            )
        )

    override suspend fun setSelectedScreenTimeouts(selectedTimeouts: List<ScreenTimeout>) =
        preferenceDataStoreHelper.putPreference(
            SELECTED_SCREEN_TIMEOUT,
            PreferencesJson.encodeToString(selectedTimeouts),
            DataStoreSourceType.DATA_SOURCE_BACKED_UP
        )

    /** Decodes the stored list, falling back to the computed default when unset or unreadable. */
    private suspend fun decodeSelectedScreenTimeouts(strList: String?): List<ScreenTimeout> =
        if (strList.isNullOrEmpty()) {
            defaultSelectedScreenTimeouts()
        } else {
            runCatching { PreferencesJson.decodeFromString<List<ScreenTimeout>>(strList) }
                .getOrElse { defaultSelectedScreenTimeouts() }
        }

    /** Fresh-install default: every catalog timeout at or above the current one. */
    private suspend fun defaultSelectedScreenTimeouts(): List<ScreenTimeout> {
        val currentTimeout = getCurrentScreenTimeout()
        return ScreenTimeoutCatalog.screenTimeouts.filter { it.value >= currentTimeout.value }
    }

    // ----- Reset when screen off -----

    override fun getResetTimeoutWhenScreenOffFlow(): Flow<Boolean> =
        preferenceDataStoreHelper.getPreference(
            RESET_TIMEOUT_WHEN_SCREEN_OFF,
            true,
            DataStoreSourceType.DATA_SOURCE_BACKED_UP
        )
            .distinctUntilChanged()

    override suspend fun getResetTimeoutWhenScreenOff(): Boolean =
        preferenceDataStoreHelper.getLastPreference(
            RESET_TIMEOUT_WHEN_SCREEN_OFF,
            true,
            DataStoreSourceType.DATA_SOURCE_BACKED_UP
        )

    override suspend fun setResetTimeoutWhenScreenOff(resetWhenScreenOff: Boolean) =
        preferenceDataStoreHelper.putPreference(
            RESET_TIMEOUT_WHEN_SCREEN_OFF,
            resetWhenScreenOff,
            DataStoreSourceType.DATA_SOURCE_BACKED_UP
        )
}
