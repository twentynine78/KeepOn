package fr.twentynine.keepon.domain.repository

import fr.twentynine.keepon.domain.model.DismissedTips
import fr.twentynine.keepon.domain.model.TimeoutIconStyle
import kotlinx.coroutines.flow.Flow

/**
 * Persistence of UI-related preferences (icon style, QS tile flag, dismissed tips).
 * The implementation is pure; legacy migration is applied by a decorator.
 */
interface UiPreferencesRepository {
    suspend fun getTimeoutIconStyleFlow(): Flow<TimeoutIconStyle>
    suspend fun getTimeoutIconStyle(): TimeoutIconStyle
    suspend fun setTimeoutIconStyle(timeoutIconStyle: TimeoutIconStyle)
    suspend fun getQSTileAddedFlow(): Flow<Boolean>
    suspend fun setQSTileAdded(isAdded: Boolean)
    suspend fun getDismissedTipsFlow(): Flow<List<DismissedTips>>
    suspend fun setDismissedTip(dismissedTips: DismissedTips)
}
