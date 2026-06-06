package fr.twentynine.keepon.domain.model

data class TipsConstraintState(
    val canPostNotification: Boolean = false,
    val servicesNotificationChannelIsDisabled: Boolean = true,
    val batteryIsNotOptimized: Boolean = false,
    val tileServiceIsAdded: Boolean = false,
    val showRateApp: Boolean = false,
)
