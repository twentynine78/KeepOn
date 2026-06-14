package fr.twentynine.keepon.data.repository

import fr.twentynine.keepon.data.enums.DataStoreSourceType
import fr.twentynine.keepon.data.local.PreferenceDataStoreConstants.DISMISSED_TIPS
import fr.twentynine.keepon.data.local.PreferenceDataStoreConstants.ICON_TRANSITION_ANIMATION
import fr.twentynine.keepon.data.local.PreferenceDataStoreConstants.QSTILE_ADDED
import fr.twentynine.keepon.data.local.PreferenceDataStoreConstants.TIMEOUT_ICON_STYLE
import fr.twentynine.keepon.data.local.PreferenceDataStoreHelper
import fr.twentynine.keepon.data.local.PreferencesJson
import fr.twentynine.keepon.domain.model.DismissedTip
import fr.twentynine.keepon.domain.model.IconTransitionAnimation
import fr.twentynine.keepon.domain.model.TimeoutIconStyle
import fr.twentynine.keepon.domain.repository.UiPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * DataStore-backed [UiPreferencesRepository]: persists the icon style, icon-change transition, the
 * dismissed-tips set and the "QS tile added" flag (JSON-encoding the model values). A stored value
 * that no longer decodes falls back to the model's defaults instead of failing the read. Legacy
 * icon-style/review fallbacks are layered on by [MigratingUiPreferencesRepository].
 */
class UiPreferencesRepositoryImpl @Inject constructor(
    private val preferenceDataStoreHelper: PreferenceDataStoreHelper,
) : UiPreferencesRepository {

    override fun getTimeoutIconStyleFlow(): Flow<TimeoutIconStyle> =
        preferenceDataStoreHelper.getPreference(
            TIMEOUT_ICON_STYLE,
            DataStoreSourceType.DATA_SOURCE_BACKED_UP
        )
            // Deduplicate the raw JSON before decoding: the store re-emits on every write to ANY
            // of its keys, and identical strings decode to equal models anyway.
            .distinctUntilChanged()
            .map(::decodeTimeoutIconStyle)

    override suspend fun getTimeoutIconStyle(): TimeoutIconStyle =
        decodeTimeoutIconStyle(
            preferenceDataStoreHelper.getLastPreference(
                TIMEOUT_ICON_STYLE,
                DataStoreSourceType.DATA_SOURCE_BACKED_UP
            )
        )

    override suspend fun setTimeoutIconStyle(timeoutIconStyle: TimeoutIconStyle) =
        preferenceDataStoreHelper.putPreference(
            TIMEOUT_ICON_STYLE,
            PreferencesJson.encodeToString(timeoutIconStyle),
            DataStoreSourceType.DATA_SOURCE_BACKED_UP
        )

    override fun getIconTransitionAnimationFlow(): Flow<IconTransitionAnimation> =
        preferenceDataStoreHelper.getPreference(
            ICON_TRANSITION_ANIMATION,
            DataStoreSourceType.DATA_SOURCE_BACKED_UP
        )
            .distinctUntilChanged()
            .map(::decodeIconTransitionAnimation)

    override suspend fun getIconTransitionAnimation(): IconTransitionAnimation =
        decodeIconTransitionAnimation(
            preferenceDataStoreHelper.getLastPreference(
                ICON_TRANSITION_ANIMATION,
                DataStoreSourceType.DATA_SOURCE_BACKED_UP
            )
        )

    override suspend fun setIconTransitionAnimation(iconTransitionAnimation: IconTransitionAnimation) =
        preferenceDataStoreHelper.putPreference(
            ICON_TRANSITION_ANIMATION,
            PreferencesJson.encodeToString(iconTransitionAnimation),
            DataStoreSourceType.DATA_SOURCE_BACKED_UP
        )

    override fun getQSTileAddedFlow(): Flow<Boolean> =
        preferenceDataStoreHelper.getPreference(
            QSTILE_ADDED,
            false,
            DataStoreSourceType.DATA_SOURCE
        )

    override suspend fun setQSTileAdded(isAdded: Boolean) =
        preferenceDataStoreHelper.putPreference(
            QSTILE_ADDED,
            isAdded,
            DataStoreSourceType.DATA_SOURCE
        )

    override fun getDismissedTipsFlow(): Flow<List<DismissedTip>> =
        preferenceDataStoreHelper.getPreference(
            DISMISSED_TIPS,
            DataStoreSourceType.DATA_SOURCE_BACKED_UP
        )
            .distinctUntilChanged()
            .map(::decodeDismissedTips)

    override suspend fun setDismissedTip(dismissedTip: DismissedTip) =
        // Atomic read-modify-write so two concurrent dismissals cannot drop one another.
        preferenceDataStoreHelper.updatePreference(
            DISMISSED_TIPS,
            DataStoreSourceType.DATA_SOURCE_BACKED_UP
        ) { storedJson ->
            PreferencesJson.encodeToString(decodeDismissedTips(storedJson) + dismissedTip)
        }

    private fun decodeTimeoutIconStyle(json: String?): TimeoutIconStyle =
        if (json.isNullOrEmpty()) {
            TimeoutIconStyle()
        } else {
            runCatching { PreferencesJson.decodeFromString<TimeoutIconStyle>(json) }
                .getOrDefault(TimeoutIconStyle())
        }

    private fun decodeIconTransitionAnimation(json: String?): IconTransitionAnimation =
        if (json.isNullOrEmpty()) {
            IconTransitionAnimation()
        } else {
            runCatching { PreferencesJson.decodeFromString<IconTransitionAnimation>(json) }
                .getOrDefault(IconTransitionAnimation())
        }

    private fun decodeDismissedTips(json: String?): List<DismissedTip> =
        if (json.isNullOrEmpty()) {
            emptyList()
        } else {
            runCatching { PreferencesJson.decodeFromString<List<DismissedTip>>(json) }
                .getOrDefault(emptyList())
        }
}
