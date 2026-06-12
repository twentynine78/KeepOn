package fr.twentynine.keepon.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.twentynine.keepon.domain.catalog.ScreenTimeoutCatalog
import fr.twentynine.keepon.ui.model.ScreenTimeoutUI
import fr.twentynine.keepon.ui.state.TaskerEditUIState
import fr.twentynine.keepon.ui.event.TaskerUIEvent
import fr.twentynine.keepon.ui.producer.BuildScreenTimeoutUiListProducer
import fr.twentynine.keepon.ui.producer.TaskerEditStateProducer
import fr.twentynine.keepon.domain.gateway.PermissionStateGateway
import fr.twentynine.keepon.domain.usecase.app.SetIsFirstLaunchUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Tasker plug-in edit activity. Exposes a [TaskerEditUIState] StateFlow (the
 * selectable timeouts plus the permission state) and tracks the user's pick, which the activity reads
 * back when saving the Tasker action.
 */
@HiltViewModel
class TaskerEditViewModel @Inject constructor(
    taskerEditStateProducer: TaskerEditStateProducer,
    permissionStateGateway: PermissionStateGateway,
    private val buildScreenTimeoutUiListProducer: BuildScreenTimeoutUiListProducer,
    private val setIsFirstLaunchUseCase: SetIsFirstLaunchUseCase,
) : ViewModel() {

    private val selectedScreenTimeoutUI: MutableStateFlow<ScreenTimeoutUI?> = MutableStateFlow(null)

    // A single eagerly built StateFlow: the producer only assembles the combine pipeline (nothing
    // runs until collection), and the upstream subscription is shared via WhileSubscribed. The
    // explicit type on catch upcasts Flow<Success> so the error state can be emitted.
    val uiState: StateFlow<TaskerEditUIState> =
        taskerEditStateProducer(
            canWriteSystemSettingFlow = permissionStateGateway.canWriteSystemSetting,
            batteryIsNotOptimizedFlow = permissionStateGateway.batteryIsNotOptimized,
            canPostNotificationFlow = permissionStateGateway.canPostNotification,
            selectedScreenTimeoutFlow = selectedScreenTimeoutUI,
        )
            .catch<TaskerEditUIState> { error -> emit(TaskerEditUIState.Error(error.message ?: error.toString())) }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                TaskerEditUIState.Loading
            )

    fun onEvent(event: TaskerUIEvent) {
        when (event) {
            TaskerUIEvent.UpdateIsFirstLaunch -> updateIsFirstLaunch()
            is TaskerUIEvent.SetSelectedScreenTimeout -> setSelectedScreenTimeout(event.screenTimeoutUI)

            // Permission request/check events are handled by the host Activity.
            TaskerUIEvent.RequestWriteSystemSettingPermission,
            TaskerUIEvent.RequestDisableBatteryOptimization,
            TaskerUIEvent.RequestPostNotification,
            TaskerUIEvent.CheckNeededPermissions -> Unit
        }
    }

    fun setInitialSelectedScreenTimeout(screenTimeout: Int) {
        viewModelScope.launch {
            val specialScreenTimeoutUI = buildScreenTimeoutUiListProducer(
                ScreenTimeoutCatalog.specialScreenTimeouts
            )
            val screenTimeoutUI = buildScreenTimeoutUiListProducer(
                ScreenTimeoutCatalog.screenTimeouts
            )

            val allScreenTimeoutList = screenTimeoutUI
                .plus(specialScreenTimeoutUI)
            val initialScreenTimeout = allScreenTimeoutList
                .firstOrNull { it.value == screenTimeout }

            initialScreenTimeout?.let { currentInitialTimeout ->
                val timeoutToSet = if (currentInitialTimeout.isLocked) {
                    allScreenTimeoutList.lastOrNull { !it.isLocked }
                } else {
                    currentInitialTimeout
                }
                timeoutToSet?.let { setSelectedScreenTimeout(it) }
            }
        }
    }

    private fun setSelectedScreenTimeout(screenTimeout: ScreenTimeoutUI) {
        selectedScreenTimeoutUI.update { screenTimeout }
    }

    private fun updateIsFirstLaunch() {
        viewModelScope.launch {
            setIsFirstLaunchUseCase(false)
        }
    }
}
