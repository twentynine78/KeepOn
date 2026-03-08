package fr.twentynine.keepon.data.model

import androidx.compose.runtime.Immutable

sealed interface WidgetUIState {
    data object Loading : WidgetUIState
    data class Error(val error: String) : WidgetUIState

    @Immutable
    data class Success(
        val currentScreenTimeout: ScreenTimeout,
        val keepOnIsActive: Boolean,
        val timeoutIconStyle: TimeoutIconStyle,
        val selectedTimeouts: List<ScreenTimeout>,
        val defaultTimeout: ScreenTimeout,
    ) : WidgetUIState
}
