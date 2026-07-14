package fr.twentynine.keepon.ui.state

import androidx.compose.runtime.Immutable
import fr.twentynine.keepon.ui.model.TaskerEventUI

/**
 * The state the Tasker event-edit screen renders: [Loading], [Error], or [Success] with the
 * selectable plug-in events, the permission flags, and the user's current pick.
 */
sealed interface TaskerEventEditUIState {
    data object Loading : TaskerEventEditUIState
    data class Error(val error: String) : TaskerEventEditUIState

    @Immutable
    data class Success(
        val canWriteSystemSettings: Boolean,
        val canPostNotification: Boolean,
        val batteryIsNotOptimized: Boolean,
        val events: List<TaskerEventUI>,
        val selectedEvent: TaskerEventUI?,
    ) : TaskerEventEditUIState
}
