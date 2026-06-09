package fr.twentynine.keepon.ui.event

import fr.twentynine.keepon.ui.model.ScreenTimeoutUI

/** The user actions the Tasker-edit screen emits to its ViewModel (permission requests, timeout selection). */
sealed interface TaskerUIEvent {
    data object RequestWriteSystemSettingPermission : TaskerUIEvent
    data object RequestDisableBatteryOptimization : TaskerUIEvent
    data object RequestPostNotification : TaskerUIEvent
    data object UpdateIsFirstLaunch : TaskerUIEvent
    data object CheckNeededPermissions : TaskerUIEvent
    data class SetSelectedScreenTimeout(val screenTimeoutUI: ScreenTimeoutUI) : TaskerUIEvent
}
