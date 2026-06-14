package fr.twentynine.keepon.data.repository

import fr.twentynine.keepon.data.enums.DataStoreSourceType
import fr.twentynine.keepon.data.local.PreferenceDataStoreConstants.DISMISSED_TIPS
import fr.twentynine.keepon.data.local.PreferenceDataStoreConstants.TIMEOUT_ICON_STYLE
import fr.twentynine.keepon.data.local.PreferenceDataStoreHelper
import fr.twentynine.keepon.data.migration.LegacyPreferencesRepository
import fr.twentynine.keepon.domain.gateway.DebugTracer
import fr.twentynine.keepon.domain.model.DismissedTip
import fr.twentynine.keepon.domain.model.IconTransitionAnimation
import fr.twentynine.keepon.domain.model.TimeoutIconStyle
import fr.twentynine.keepon.domain.repository.UiPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Migrating decorator over [UiPreferencesRepositoryImpl].
 *
 * Lazily migrates the deprecated icon-style model and the "appReviewAsked" flag into
 * their current representations on first use (first collection of their flows, or first
 * one-shot read), serialized by a mutex so concurrent first reads cannot interleave the
 * check-then-act.
 */
class MigratingUiPreferencesRepository @Inject constructor(
    private val delegate: UiPreferencesRepositoryImpl,
    private val legacyPreferencesRepository: LegacyPreferencesRepository,
    private val preferenceDataStoreHelper: PreferenceDataStoreHelper,
    private val tracer: DebugTracer,
) : UiPreferencesRepository {

    private val ioDispatcher = Dispatchers.IO

    // Serializes the lazy migrations: without it a write landing between the
    // "current is unset" check and the migration write could be clobbered.
    private val migrationMutex = Mutex()

    // Fast paths: once a migration has been checked once, later uses skip the DataStore
    // read entirely. Safe because the repository is bound as a @Singleton.
    @Volatile private var timeoutIconStyleMigrated = false

    @Volatile private var dismissedTipsMigrated = false

    override fun getTimeoutIconStyleFlow(): Flow<TimeoutIconStyle> = flow {
        migrateTimeoutIconStyleIfNeeded()
        emitAll(delegate.getTimeoutIconStyleFlow())
    }

    override suspend fun getTimeoutIconStyle(): TimeoutIconStyle {
        migrateTimeoutIconStyleIfNeeded()
        return delegate.getTimeoutIconStyle()
    }

    override fun getDismissedTipsFlow(): Flow<List<DismissedTip>> = flow {
        migrateDismissedTipsIfNeeded()
        emitAll(delegate.getDismissedTipsFlow())
    }

    private suspend fun migrateTimeoutIconStyleIfNeeded() {
        if (timeoutIconStyleMigrated) return
        withContext(ioDispatcher) {
            migrationMutex.withLock {
                if (timeoutIconStyleMigrated) return@withLock
                val current = preferenceDataStoreHelper.getLastPreference(
                    TIMEOUT_ICON_STYLE,
                    DataStoreSourceType.DATA_SOURCE_BACKED_UP
                )
                if (current.isNullOrEmpty()) {
                    val old = legacyPreferencesRepository.getOldTimeoutIconStyle()
                    if (old != null) {
                        tracer.trace(TAG) { "migrating legacy timeout-icon style" }
                        delegate.setTimeoutIconStyle(old.toTimeoutIconStyle)
                        legacyPreferencesRepository.removeOldTimeoutIconStyle()
                    }
                }
                timeoutIconStyleMigrated = true
            }
        }
    }

    private suspend fun migrateDismissedTipsIfNeeded() {
        if (dismissedTipsMigrated) return
        withContext(ioDispatcher) {
            migrationMutex.withLock {
                if (dismissedTipsMigrated) return@withLock
                val current = preferenceDataStoreHelper.getLastPreference(
                    DISMISSED_TIPS,
                    DataStoreSourceType.DATA_SOURCE_BACKED_UP
                )
                if (current.isNullOrEmpty() && legacyPreferencesRepository.getOldAppReviewAsked()) {
                    tracer.trace(TAG) { "migrating legacy appReviewAsked flag into the dismissed rate tip" }
                    delegate.setDismissedTip(DismissedTip(RATE_APP_TIP_ID))
                    legacyPreferencesRepository.removeOldAppReviewAsked()
                }
                dismissedTipsMigrated = true
            }
        }
    }

    override suspend fun setTimeoutIconStyle(timeoutIconStyle: TimeoutIconStyle) =
        delegate.setTimeoutIconStyle(timeoutIconStyle)

    // No legacy representation for the icon transition (introduced fresh) — pass through.
    override fun getIconTransitionAnimationFlow(): Flow<IconTransitionAnimation> =
        delegate.getIconTransitionAnimationFlow()

    override suspend fun getIconTransitionAnimation(): IconTransitionAnimation =
        delegate.getIconTransitionAnimation()

    override suspend fun setIconTransitionAnimation(iconTransitionAnimation: IconTransitionAnimation) =
        delegate.setIconTransitionAnimation(iconTransitionAnimation)

    override fun getQSTileAddedFlow(): Flow<Boolean> =
        delegate.getQSTileAddedFlow()

    override suspend fun setQSTileAdded(isAdded: Boolean) =
        delegate.setQSTileAdded(isAdded)

    override suspend fun setDismissedTip(dismissedTip: DismissedTip) =
        delegate.setDismissedTip(dismissedTip)

    private companion object {
        // Persisted id of the "rate app" tip (TipInfo.RateApp.id); a stored DismissedTip
        // id can never change, so it is safe to reference by value from the data layer.
        const val RATE_APP_TIP_ID = 300
        const val TAG = "Migration"
    }
}

