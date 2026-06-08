package fr.twentynine.keepon.ui.state

import androidx.compose.runtime.Immutable
import fr.twentynine.keepon.domain.model.IconTransitionAnimation
import fr.twentynine.keepon.domain.model.ScreenTimeout
import fr.twentynine.keepon.domain.model.TimeoutIconStyle

sealed interface WidgetUIState {
    data object Loading : WidgetUIState
    data class Error(val error: String) : WidgetUIState

    @Immutable
    data class Success(
        val currentScreenTimeout: ScreenTimeout,
        val keepOnIsActive: Boolean,
        val timeoutIconStyle: TimeoutIconStyle,
        val canCycleTimeout: Boolean,
        val iconTransitionAnimation: IconTransitionAnimation = IconTransitionAnimation(),
    ) : WidgetUIState
}
