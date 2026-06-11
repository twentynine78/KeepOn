package fr.twentynine.keepon.data.repository

import fr.twentynine.keepon.data.enums.DataStoreSourceType
import fr.twentynine.keepon.data.local.PreferenceDataStoreConstants.DISMISSED_TIPS
import fr.twentynine.keepon.data.local.PreferenceDataStoreConstants.ICON_TRANSITION_ANIMATION
import fr.twentynine.keepon.data.local.PreferenceDataStoreConstants.QSTILE_ADDED
import fr.twentynine.keepon.data.local.PreferenceDataStoreConstants.TIMEOUT_ICON_STYLE
import fr.twentynine.keepon.data.local.PreferenceDataStoreHelper
import fr.twentynine.keepon.domain.model.DismissedTips
import fr.twentynine.keepon.domain.model.IconTransitionAnimation
import fr.twentynine.keepon.domain.model.TimeoutIconStyle
import fr.twentynine.keepon.domain.repository.UiPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject

/**
 * DataStore-backed [UiPreferencesRepository]: persists the icon style, icon-change transition, the
 * dismissed-tips set and the "QS tile added" flag, all on the IO dispatcher (JSON-encoding the model
 * values). Legacy icon-style/review fallbacks are layered on by [MigratingUiPreferencesRepository].
 */
class UiPreferencesRepositoryImpl @Inject constructor(
    private val preferenceDataStoreHelper: PreferenceDataStoreHelper,
) : UiPreferencesRepository {

    private val ioDispatcher = Dispatchers.IO

    override fun getTimeoutIconStyleFlow(): Flow<TimeoutIconStyle> =
        preferenceDataStoreHelper.getPreference(
            TIMEOUT_ICON_STYLE,
            DataStoreSourceType.DATA_SOURCE_BACKED_UP
        )
            .map { timeoutIconStyleJson ->
                if (timeoutIconStyleJson.isNullOrEmpty()) {
                    TimeoutIconStyle()
                } else {
                    Json.decodeFromString<TimeoutIconStyle>(timeoutIconStyleJson)
                }
            }
            .distinctUntilChanged()

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

    override fun getIconTransitionAnimationFlow(): Flow<IconTransitionAnimation> =
        preferenceDataStoreHelper.getPreference(
            ICON_TRANSITION_ANIMATION,
            DataStoreSourceType.DATA_SOURCE_BACKED_UP
        )
            .map { iconTransitionAnimationJson ->
                if (iconTransitionAnimationJson.isNullOrEmpty()) {
                    IconTransitionAnimation()
                } else {
                    Json.decodeFromString<IconTransitionAnimation>(iconTransitionAnimationJson)
                }
            }
            .distinctUntilChanged()

    override suspend fun getIconTransitionAnimation(): IconTransitionAnimation =
        withContext(ioDispatcher) {
            val iconTransitionAnimationJson = preferenceDataStoreHelper.getLastPreference(
                ICON_TRANSITION_ANIMATION,
                DataStoreSourceType.DATA_SOURCE_BACKED_UP
            )
            if (iconTransitionAnimationJson.isNullOrEmpty()) {
                IconTransitionAnimation()
            } else {
                Json.decodeFromString<IconTransitionAnimation>(iconTransitionAnimationJson)
            }
        }

    override suspend fun setIconTransitionAnimation(iconTransitionAnimation: IconTransitionAnimation) =
        withContext(ioDispatcher) {
            preferenceDataStoreHelper.putPreference(
                ICON_TRANSITION_ANIMATION,
                Json.encodeToString(iconTransitionAnimation),
                DataStoreSourceType.DATA_SOURCE_BACKED_UP
            )
        }

    override fun getQSTileAddedFlow(): Flow<Boolean> =
        preferenceDataStoreHelper.getPreference(
            QSTILE_ADDED,
            false,
            DataStoreSourceType.DATA_SOURCE
        )

    override suspend fun setQSTileAdded(isAdded: Boolean) =
        withContext(ioDispatcher) {
            preferenceDataStoreHelper.putPreference(
                QSTILE_ADDED,
                isAdded,
                DataStoreSourceType.DATA_SOURCE
            )
        }

    override fun getDismissedTipsFlow(): Flow<List<DismissedTips>> =
        preferenceDataStoreHelper.getPreference(
            DISMISSED_TIPS,
            DataStoreSourceType.DATA_SOURCE_BACKED_UP
        )
            .map { dismissedTipsStr ->
                if (dismissedTipsStr.isNullOrEmpty()) {
                    emptyList()
                } else {
                    Json.decodeFromString<List<DismissedTips>>(dismissedTipsStr)
                }
            }
            .distinctUntilChanged()

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
