package fr.twentynine.keepon.data.repository

import fr.twentynine.keepon.data.enums.DataStoreSourceType
import fr.twentynine.keepon.data.local.PreferenceDataStoreConstants.DISMISSED_TIPS
import fr.twentynine.keepon.data.local.PreferenceDataStoreConstants.TIMEOUT_ICON_STYLE
import fr.twentynine.keepon.data.local.PreferenceDataStoreHelper
import fr.twentynine.keepon.data.migration.LegacyPreferencesRepository
import fr.twentynine.keepon.domain.model.DismissedTips
import fr.twentynine.keepon.domain.model.IconTransitionAnimation
import fr.twentynine.keepon.domain.model.TimeoutIconStyle
import fr.twentynine.keepon.domain.repository.UiPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Migrating decorator over [UiPreferencesRepositoryImpl].
 *
 * Lazily migrates the deprecated icon-style model and the "appReviewAsked" flag
 * into their current representations on first read. Migration stays lazy
 * (atomic per read) — no startup race.
 */
class MigratingUiPreferencesRepository @Inject constructor(
    private val delegate: UiPreferencesRepositoryImpl,
    private val legacyPreferencesRepository: LegacyPreferencesRepository,
    private val preferenceDataStoreHelper: PreferenceDataStoreHelper,
) : UiPreferencesRepository {

    private val ioDispatcher = Dispatchers.IO

    override suspend fun getTimeoutIconStyleFlow(): Flow<TimeoutIconStyle> {
        migrateTimeoutIconStyleIfNeeded()
        return delegate.getTimeoutIconStyleFlow()
    }

    override suspend fun getTimeoutIconStyle(): TimeoutIconStyle {
        migrateTimeoutIconStyleIfNeeded()
        return delegate.getTimeoutIconStyle()
    }

    override suspend fun getDismissedTipsFlow(): Flow<List<DismissedTips>> {
        migrateDismissedTipsIfNeeded()
        return delegate.getDismissedTipsFlow()
    }

    private suspend fun migrateTimeoutIconStyleIfNeeded() =
        withContext(ioDispatcher) {
            val current = preferenceDataStoreHelper.getLastPreference(
                TIMEOUT_ICON_STYLE,
                DataStoreSourceType.DATA_SOURCE_BACKED_UP
            )
            if (current.isNullOrEmpty()) {
                val old = legacyPreferencesRepository.getOldTimeoutIconStyle()
                if (old != null) {
                    delegate.setTimeoutIconStyle(old.toTimeoutIconStyle)
                    legacyPreferencesRepository.removeOldTimeoutIconStyle()
                }
            }
        }

    private suspend fun migrateDismissedTipsIfNeeded() =
        withContext(ioDispatcher) {
            val current = preferenceDataStoreHelper.getLastPreference(
                DISMISSED_TIPS,
                DataStoreSourceType.DATA_SOURCE_BACKED_UP
            )
            if (current.isNullOrEmpty() && legacyPreferencesRepository.getOldAppReviewAsked()) {
                delegate.setDismissedTip(DismissedTips(RATE_APP_TIP_ID))
                legacyPreferencesRepository.removeOldAppReviewAsked()
            }
        }

    override suspend fun setTimeoutIconStyle(timeoutIconStyle: TimeoutIconStyle) =
        delegate.setTimeoutIconStyle(timeoutIconStyle)

    // No legacy representation for the icon transition (introduced fresh) — pass through.
    override suspend fun getIconTransitionAnimationFlow(): Flow<IconTransitionAnimation> =
        delegate.getIconTransitionAnimationFlow()

    override suspend fun getIconTransitionAnimation(): IconTransitionAnimation =
        delegate.getIconTransitionAnimation()

    override suspend fun setIconTransitionAnimation(iconTransitionAnimation: IconTransitionAnimation) =
        delegate.setIconTransitionAnimation(iconTransitionAnimation)

    override suspend fun getQSTileAddedFlow(): Flow<Boolean> =
        delegate.getQSTileAddedFlow()

    override suspend fun setQSTileAdded(isAdded: Boolean) =
        delegate.setQSTileAdded(isAdded)

    override suspend fun setDismissedTip(dismissedTips: DismissedTips) =
        delegate.setDismissedTip(dismissedTips)

    private companion object {
        // Persisted id of the "rate app" tip (TipsInfo.RateApp.id); a stored DismissedTips
        // id can never change, so it is safe to reference by value from the data layer.
        const val RATE_APP_TIP_ID = 300
    }
}

