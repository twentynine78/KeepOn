package fr.twentynine.keepon.ui.state

import androidx.compose.runtime.Immutable
import fr.twentynine.keepon.data.model.ScreenTimeout
import fr.twentynine.keepon.data.model.ScreenTimeoutUI
import fr.twentynine.keepon.data.model.TimeoutIconStyle

sealed interface TaskerEditUIState {
    data object Loading : TaskerEditUIState
    data class Error(val error: String) : TaskerEditUIState

    @Immutable
    data class Success(
        val canWriteSystemSettings: Boolean,
        val canPostNotification: Boolean,
        val batteryIsNotOptimized: Boolean,
        val defaultScreenTimeout: ScreenTimeout,
        val previousScreenTimeout: ScreenTimeout,
        val timeoutIconStyle: TimeoutIconStyle,
        val specialScreenTimeouts: List<ScreenTimeoutUI>,
        val screenTimeouts: List<ScreenTimeoutUI>,
        val selectedScreenTimeout: ScreenTimeoutUI?,
    ) : TaskerEditUIState
}
