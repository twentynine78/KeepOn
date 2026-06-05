package fr.twentynine.keepon.ui.state

import androidx.compose.runtime.Immutable
import fr.twentynine.keepon.data.catalog.TipsInfo
import fr.twentynine.keepon.data.model.ScreenTimeout
import fr.twentynine.keepon.data.model.ScreenTimeoutUI
import fr.twentynine.keepon.data.model.TimeoutIconStyle

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
        val keepOnIsActive: Boolean,
        val timeoutIconStyle: TimeoutIconStyle,
        val isFirstLaunch: Boolean,
    ) : MainViewUIState
}
