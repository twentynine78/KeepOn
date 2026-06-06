package fr.twentynine.keepon.data.repository

import fr.twentynine.keepon.data.enums.DataStoreSourceType
import fr.twentynine.keepon.data.local.PreferenceDataStoreConstants.DISMISSED_TIPS
import fr.twentynine.keepon.data.local.PreferenceDataStoreConstants.QSTILE_ADDED
import fr.twentynine.keepon.data.local.PreferenceDataStoreConstants.TIMEOUT_ICON_STYLE
import fr.twentynine.keepon.data.local.PreferenceDataStoreHelper
import fr.twentynine.keepon.domain.model.DismissedTips
import fr.twentynine.keepon.domain.model.TimeoutIconStyle
import fr.twentynine.keepon.domain.repository.UiPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject

class UiPreferencesRepositoryImpl @Inject constructor(
    private val preferenceDataStoreHelper: PreferenceDataStoreHelper,
) : UiPreferencesRepository {

    private val ioDispatcher = Dispatchers.IO

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun getTimeoutIconStyleFlow(): Flow<TimeoutIconStyle> =
        withContext(ioDispatcher) {
            preferenceDataStoreHelper.getPreference(
                TIMEOUT_ICON_STYLE,
                DataStoreSourceType.DATA_SOURCE_BACKED_UP
            )
                .transformLatest { timeoutIconStyleJson ->
                    emit(
                        if (timeoutIconStyleJson.isNullOrEmpty()) {
                            TimeoutIconStyle()
                        } else {
                            Json.decodeFromString<TimeoutIconStyle>(timeoutIconStyleJson)
                        }
                    )
                }
                .distinctUntilChanged()
        }

    override suspend fun getTimeoutIconStyle(): TimeoutIconStyle =
        withContext(ioDispatcher) {
            val timeoutIconStyleJson = preferenceDataStoreHelper.getLastPreference(
                TIMEOUT_ICON_STYLE,
                DataStoreSourceType.DATA_SOURCE_BACKED_UP
            )
            if (timeoutIconStyleJson.isNullOrEmpty()) {
                TimeoutIconStyle()
            } else {
                Json.decodeFromString<TimeoutIconStyle>(timeoutIconStyleJson)
            }
        }

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

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun getDismissedTipsFlow(): Flow<List<DismissedTips>> =
        withContext(ioDispatcher) {
            preferenceDataStoreHelper.getPreference(
                DISMISSED_TIPS,
                DataStoreSourceType.DATA_SOURCE_BACKED_UP
            )
                .transformLatest { dismissedTipsStr ->
                    emit(
                        if (dismissedTipsStr.isNullOrEmpty()) {
                            emptyList()
                        } else {
                            Json.decodeFromString<List<DismissedTips>>(dismissedTipsStr)
                        }
                    )
                }
                .distinctUntilChanged()
        }

    override suspend fun setDismissedTip(dismissedTips: DismissedTips) =
        withContext(ioDispatcher) {
            val dismissedTipList = getDismissedTips().toMutableList()
            dismissedTipList.add(dismissedTips)
            setDismissedTips(dismissedTipList)
        }

    private suspend fun getDismissedTips(): List<DismissedTips> =
        withContext(ioDispatcher) {
            val dismissedTipsStr = preferenceDataStoreHelper.getLastPreference(
                DISMISSED_TIPS,
                DataStoreSourceType.DATA_SOURCE_BACKED_UP
            )
            if (dismissedTipsStr.isNullOrEmpty()) {
                emptyList()
            } else {
                Json.decodeFromString<List<DismissedTips>>(dismissedTipsStr)
            }
        }

    private suspend fun setDismissedTips(dismissedTips: List<DismissedTips>) =
        withContext(ioDispatcher) {
            preferenceDataStoreHelper.putPreference(
                DISMISSED_TIPS,
                Json.encodeToString<List<DismissedTips>>(dismissedTips),
                DataStoreSourceType.DATA_SOURCE_BACKED_UP
            )
        }
}
