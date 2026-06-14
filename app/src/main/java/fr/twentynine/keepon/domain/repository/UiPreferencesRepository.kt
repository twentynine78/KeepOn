package fr.twentynine.keepon.domain.repository

import fr.twentynine.keepon.domain.model.DismissedTip
import fr.twentynine.keepon.domain.model.IconTransitionAnimation
import fr.twentynine.keepon.domain.model.TimeoutIconStyle
import kotlinx.coroutines.flow.Flow

/**
 * Persistence of UI-related preferences (icon style, QS tile flag, dismissed tips).
 * The implementation is pure; legacy migration is applied by a decorator.
 */
interface UiPreferencesRepository {
    fun getTimeoutIconStyleFlow(): Flow<TimeoutIconStyle>
    suspend fun getTimeoutIconStyle(): TimeoutIconStyle
    suspend fun setTimeoutIconStyle(timeoutIconStyle: TimeoutIconStyle)
    fun getIconTransitionAnimationFlow(): Flow<IconTransitionAnimation>
    suspend fun getIconTransitionAnimation(): IconTransitionAnimation
    suspend fun setIconTransitionAnimation(iconTransitionAnimation: IconTransitionAnimation)
    fun getQSTileAddedFlow(): Flow<Boolean>
    suspend fun setQSTileAdded(isAdded: Boolean)
    fun getDismissedTipsFlow(): Flow<List<DismissedTip>>
    suspend fun setDismissedTip(dismissedTip: DismissedTip)
}
