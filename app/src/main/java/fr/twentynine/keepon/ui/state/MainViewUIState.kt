package fr.twentynine.keepon.ui.state

import androidx.compose.runtime.Immutable
import fr.twentynine.keepon.ui.catalog.TipsInfo
import fr.twentynine.keepon.domain.model.AppInfo
import fr.twentynine.keepon.domain.model.IconTransitionAnimation
import fr.twentynine.keepon.domain.model.ScreenTimeout
import fr.twentynine.keepon.ui.model.ScreenTimeoutUI
import fr.twentynine.keepon.domain.model.TimeoutIconStyle

sealed interface MainViewUIState {
    data object Loading : MainViewUIState
    data class Error(val error: String) : MainViewUIState

    @Immutable
    data class Success(
        val canWriteSystemSettings: Boolean,
        val canPostNotification: Boolean,
        val batteryIsNotOptimized: Boolean,
        val screenTimeouts: List<ScreenTimeoutUI>,
        val tipsList: List<TipsInfo>,
        val resetTimeoutWhenScreenOff: Boolean,
        val currentScreenTimeout: ScreenTimeout,
        val currentTimeoutDisplay: String,
        val keepOnIsActive: Boolean,
        val timeoutIconStyle: TimeoutIconStyle,
        val iconTransitionAnimation: IconTransitionAnimation,
        val isFirstLaunch: Boolean,
        val appInfo: AppInfo,
    ) : MainViewUIState
}
