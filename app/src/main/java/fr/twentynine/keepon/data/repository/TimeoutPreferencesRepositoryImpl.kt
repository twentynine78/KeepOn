package fr.twentynine.keepon.data.repository

import fr.twentynine.keepon.data.enums.DataStoreSourceType
import fr.twentynine.keepon.data.local.PreferenceDataStoreConstants.CURRENT_SCREEN_TIMEOUT
import fr.twentynine.keepon.data.local.PreferenceDataStoreConstants.PREVIOUS_SCREEN_TIMEOUT
import fr.twentynine.keepon.data.local.PreferenceDataStoreConstants.SELECTED_SCREEN_TIMEOUT
import fr.twentynine.keepon.data.local.PreferenceDataStoreHelper
import fr.twentynine.keepon.domain.gateway.SystemScreenTimeoutController
import fr.twentynine.keepon.domain.model.ScreenTimeout
import fr.twentynine.keepon.domain.repository.TimeoutPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject

class TimeoutPreferencesRepositoryImpl @Inject constructor(
    private val preferenceDataStoreHelper: PreferenceDataStoreHelper,
    private val systemScreenTimeoutController: SystemScreenTimeoutController,
) : TimeoutPreferencesRepository {

    private val ioDispatcher = Dispatchers.IO

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun getCurrentScreenTimeoutFlow(): Flow<ScreenTimeout> =
        withContext(ioDispatcher) {
            val defaultValue = systemScreenTimeoutController.getSystemScreenTimeout()
            preferenceDataStoreHelper.getPreference(
                CURRENT_SCREEN_TIMEOUT,
                defaultValue.value,
                DataStoreSourceType.DATA_SOURCE
            )
                .transformLatest { emit(ScreenTimeout(it)) }
                .distinctUntilChanged()
        }

    override suspend fun getCurrentScreenTimeout(): ScreenTimeout =
        withContext(ioDispatcher) {
            val defaultValue = systemScreenTimeoutController.getSystemScreenTimeout()

            ScreenTimeout(
                preferenceDataStoreHelper.getLastPreference(
                    CURRENT_SCREEN_TIMEOUT,
                    defaultValue.value,
                    DataStoreSourceType.DATA_SOURCE
                )
            )
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun getPreviousScreenTimeoutFlow(): Flow<ScreenTimeout> =
        withContext(ioDispatcher) {
            val defaultValue = -1
            preferenceDataStoreHelper.getPreference(
                PREVIOUS_SCREEN_TIMEOUT,
                defaultValue,
                DataStoreSourceType.DATA_SOURCE
            )
                .transformLatest { emit(ScreenTimeout(it)) }
                .distinctUntilChanged()
        }

    override suspend fun getPreviousScreenTimeout(): ScreenTimeout =
        withContext(ioDispatcher) {
            val defaultValue = -1
            ScreenTimeout(
                preferenceDataStoreHelper.getLastPreference(
                    PREVIOUS_SCREEN_TIMEOUT,
                    defaultValue,
                    DataStoreSourceType.DATA_SOURCE
                )
            )
        }

    override suspend fun setPreviousScreenTimeout(timeout: ScreenTimeout) =
        withContext(ioDispatcher) {
            preferenceDataStoreHelper.putPreference(
                PREVIOUS_SCREEN_TIMEOUT,
                timeout.value,
                DataStoreSourceType.DATA_SOURCE
            )
        }

    override suspend fun setSelectedScreenTimeouts(selectedTimeouts: List<ScreenTimeout>) =
        withContext(ioDispatcher) {
            preferenceDataStoreHelper.putPreference(
                SELECTED_SCREEN_TIMEOUT,
                Json.encodeToString(selectedTimeouts),
                DataStoreSourceType.DATA_SOURCE_BACKED_UP
            )
        }
}
