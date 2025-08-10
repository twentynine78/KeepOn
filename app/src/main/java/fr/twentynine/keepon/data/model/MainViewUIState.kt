package fr.twentynine.keepon.data.model

import androidx.compose.runtime.Immutable
import fr.twentynine.keepon.data.local.TipsInfo

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
