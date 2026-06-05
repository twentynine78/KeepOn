package fr.twentynine.keepon.ui.event

import fr.twentynine.keepon.data.model.ScreenTimeoutUI

sealed interface TaskerUIEvent {
    data object RequestWriteSystemSettingPermission : TaskerUIEvent
    data object RequestDisableBatteryOptimization : TaskerUIEvent
    data object RequestPostNotification : TaskerUIEvent
    data object UpdateIsFirstLaunch : TaskerUIEvent
    data object CheckNeededPermissions : TaskerUIEvent
    data class SetSelectedScreenTimeout(val screenTimeoutUI: ScreenTimeoutUI) : TaskerUIEvent
}
