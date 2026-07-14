package fr.twentynine.keepon.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.twentynine.keepon.domain.gateway.PermissionStateGateway
import fr.twentynine.keepon.ui.event.TaskerEventUIEvent
import fr.twentynine.keepon.ui.model.TaskerEventUI
import fr.twentynine.keepon.ui.producer.BuildTaskerEventUiListProducer
import fr.twentynine.keepon.ui.producer.TaskerEventEditStateProducer
import fr.twentynine.keepon.ui.state.TaskerEventEditUIState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * ViewModel for the Tasker plug-in event-edit activity. Exposes a [TaskerEventEditUIState]
 * StateFlow (the selectable plug-in events plus the permission state) and tracks the user's pick,
 * which the activity reads back when saving the Tasker condition.
 */
@HiltViewModel
class TaskerEventEditViewModel @Inject constructor(
    taskerEventEditStateProducer: TaskerEventEditStateProducer,
    permissionStateGateway: PermissionStateGateway,
    private val buildTaskerEventUiListProducer: BuildTaskerEventUiListProducer,
) : ViewModel() {

    private val selectedEventUI: MutableStateFlow<TaskerEventUI?> = MutableStateFlow(null)

    // A single eagerly built StateFlow: the producer only assembles the combine pipeline (nothing
    // runs until collection), and the upstream subscription is shared via WhileSubscribed. The
    // explicit type on catch upcasts Flow<Success> so the error state can be emitted.
    val uiState: StateFlow<TaskerEventEditUIState> =
        taskerEventEditStateProducer(
            canWriteSystemSettingFlow = permissionStateGateway.canWriteSystemSetting,
            batteryIsNotOptimizedFlow = permissionStateGateway.batteryIsNotOptimized,
            canPostNotificationFlow = permissionStateGateway.canPostNotification,
            selectedEventFlow = selectedEventUI,
        )
            .catch<TaskerEventEditUIState> { error -> emit(TaskerEventEditUIState.Error(error.message ?: error.toString())) }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                TaskerEventEditUIState.Loading
            )

    fun onEvent(event: TaskerEventUIEvent) {
        when (event) {
            is TaskerEventUIEvent.SetSelectedEvent -> setSelectedEvent(event.taskerEventUI)

            // Permission request/check events are handled by the host Activity.
            TaskerEventUIEvent.RequestWriteSystemSettingPermission,
            TaskerEventUIEvent.RequestDisableBatteryOptimization,
            TaskerEventUIEvent.RequestPostNotification,
            TaskerEventUIEvent.CheckNeededPermissions -> Unit
        }
    }

    /** Preselects the event forwarded by the host; an unknown id (or the missing-extra -1) matches nothing. */
    fun setInitialSelectedEvent(eventId: Int) {
        // onCreate re-invokes this with the original intent on every configuration change: never
        // clobber a selection the user already made in this session.
        if (selectedEventUI.value != null) return

        buildTaskerEventUiListProducer()
            .firstOrNull { it.type.id == eventId }
            ?.let { setSelectedEvent(it) }
    }

    private fun setSelectedEvent(event: TaskerEventUI) {
        selectedEventUI.update { event }
    }
}
