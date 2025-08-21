package fr.twentynine.keepon.data.model

sealed interface TaskerUIEvent {
    data object RequestWriteSystemSettingPermission : TaskerUIEvent
    data object RequestDisableBatteryOptimization : TaskerUIEvent
    data object RequestPostNotification : TaskerUIEvent
    data object UpdateIsFirstLaunch : TaskerUIEvent
    data object CheckNeededPermissions : TaskerUIEvent
    data class SetSelectedScreenTimeout(val screenTimeoutUI: ScreenTimeoutUI) : TaskerUIEvent
}
