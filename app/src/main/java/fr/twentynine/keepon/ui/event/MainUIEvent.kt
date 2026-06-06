package fr.twentynine.keepon.ui.event

import fr.twentynine.keepon.domain.model.ScreenTimeout
import fr.twentynine.keepon.ui.model.ScreenTimeoutUI
import fr.twentynine.keepon.domain.model.TimeoutIconStyle

sealed interface MainUIEvent {
    data object RequestWriteSystemSettingPermission : MainUIEvent
    data object RequestDisableBatteryOptimization : MainUIEvent
    data object SetNextSelectedSystemScreenTimeout : MainUIEvent
    data object UpdateIsFirstLaunch : MainUIEvent
    data object RequestPostNotification : MainUIEvent
    data object RequestAddTileService : MainUIEvent
    data object RequestAppRate : MainUIEvent
    data object CheckNeededPermissions : MainUIEvent
    data object IncrementAppLaunchCount : MainUIEvent
    data class SetResetTimeoutWhenScreenOff(val resetTimeoutWhenScreenOff: Boolean) : MainUIEvent
    data class ToggleScreenTimeoutSelection(val screenTimeoutUI: ScreenTimeoutUI) : MainUIEvent
    data class SetDefaultScreenTimeout(val timeout: ScreenTimeout) : MainUIEvent
    data class UpdateTimeoutIconStyle(val timeoutIconStyle: TimeoutIconStyle) : MainUIEvent
    data class DismissTips(val tipsId: Int) : MainUIEvent
}
