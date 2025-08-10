package fr.twentynine.keepon.data.model

import androidx.compose.runtime.Immutable

@Immutable
data class TipsConstraintState(
    val canPostNotification: Boolean = false,
    val servicesNotificationChannelIsDisabled: Boolean = true,
    val batteryIsNotOptimized: Boolean = false,
    val tileServiceIsAdded: Boolean = false,
    val showRateApp: Boolean = false,
)
