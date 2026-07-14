package fr.twentynine.keepon.ui.event

import fr.twentynine.keepon.ui.model.TaskerEventUI

/** The user actions the Tasker event-edit screen emits to its ViewModel (permission requests, event selection). */
sealed interface TaskerEventUIEvent {
    data object RequestWriteSystemSettingPermission : TaskerEventUIEvent
    data object RequestDisableBatteryOptimization : TaskerEventUIEvent
    data object RequestPostNotification : TaskerEventUIEvent
    data object CheckNeededPermissions : TaskerEventUIEvent
    data class SetSelectedEvent(val taskerEventUI: TaskerEventUI) : TaskerEventUIEvent
}
