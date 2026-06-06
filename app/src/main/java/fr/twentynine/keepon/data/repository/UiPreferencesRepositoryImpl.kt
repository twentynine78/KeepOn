package fr.twentynine.keepon.data.repository

import fr.twentynine.keepon.data.enums.DataStoreSourceType
import fr.twentynine.keepon.data.local.PreferenceDataStoreConstants.QSTILE_ADDED
import fr.twentynine.keepon.data.local.PreferenceDataStoreConstants.TIMEOUT_ICON_STYLE
import fr.twentynine.keepon.data.local.PreferenceDataStoreHelper
import fr.twentynine.keepon.domain.model.TimeoutIconStyle
import fr.twentynine.keepon.domain.repository.UiPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject

class UiPreferencesRepositoryImpl @Inject constructor(
    private val preferenceDataStoreHelper: PreferenceDataStoreHelper,
) : UiPreferencesRepository {

    private val ioDispatcher = Dispatchers.IO

    override suspend fun setTimeoutIconStyle(timeoutIconStyle: TimeoutIconStyle) =
        withContext(ioDispatcher) {
            preferenceDataStoreHelper.putPreference(
                TIMEOUT_ICON_STYLE,
                Json.encodeToString(timeoutIconStyle),
                DataStoreSourceType.DATA_SOURCE_BACKED_UP
            )
        }

    override suspend fun getQSTileAddedFlow(): Flow<Boolean> =
        withContext(ioDispatcher) {
            val defaultValue = false
            preferenceDataStoreHelper.getPreference(
                QSTILE_ADDED,
                defaultValue,
                DataStoreSourceType.DATA_SOURCE
            )
        }

    override suspend fun setQSTileAdded(isAdded: Boolean) =
        withContext(ioDispatcher) {
            preferenceDataStoreHelper.putPreference(
                QSTILE_ADDED,
                isAdded,
                DataStoreSourceType.DATA_SOURCE
            )
        }
}
