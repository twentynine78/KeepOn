package fr.twentynine.keepon.ui.state

import androidx.compose.runtime.Immutable
import fr.twentynine.keepon.ui.catalog.TipInfo
import fr.twentynine.keepon.domain.model.AppInfo
import fr.twentynine.keepon.domain.model.IconTransitionAnimation
import fr.twentynine.keepon.domain.model.ScreenTimeout
import fr.twentynine.keepon.ui.model.CreditSectionUI
import fr.twentynine.keepon.ui.model.IconTransitionOptionUI
import fr.twentynine.keepon.ui.model.ScreenTimeoutUI
import fr.twentynine.keepon.domain.model.TimeoutIconStyle

/**
 * The immutable UI state the main activity renders: [Loading] before the first emission, [Error] on a
 * failure, and [Success] holding everything the screens need (permission flags, the timeout lists and
 * current value, tips, icon style/transition, app info and credits).
 */
sealed interface MainViewUIState {
    data object Loading : MainViewUIState
    data class Error(val error: String) : MainViewUIState

    @Immutable
    data class Success(
        val canWriteSystemSettings: Boolean,
        val canPostNotification: Boolean,
        val batteryIsNotOptimized: Boolean,
        val screenTimeouts: List<ScreenTimeoutUI>,
        val tipsList: List<TipInfo>,
        val resetTimeoutWhenScreenOff: Boolean,
        val currentScreenTimeout: ScreenTimeout,
        val currentTimeoutDisplay: String,
        val keepOnIsActive: Boolean,
        val timeoutIconStyle: TimeoutIconStyle,
        val iconTransitionAnimation: IconTransitionAnimation,
        val iconTransitionOptions: List<IconTransitionOptionUI>,
        val showFirstLaunchHint: Boolean,
        val stylePositionPadExpanded: Boolean,
        val appInfo: AppInfo,
        val creditSections: List<CreditSectionUI>,
    ) : MainViewUIState
}
