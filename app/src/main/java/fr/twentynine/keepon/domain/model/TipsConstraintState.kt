package fr.twentynine.keepon.domain.model

/**
 * The device/permission conditions a tip can be gated on. The tips catalog matches each tip's
 * required constraints against this snapshot to decide which tips are currently relevant (e.g. only
 * show the battery-optimization tip while [batteryIsNotOptimized]).
 */
data class TipsConstraintState(
    val canPostNotification: Boolean = false,
    val servicesNotificationChannelIsDisabled: Boolean = true,
    val batteryIsNotOptimized: Boolean = false,
    val tileServiceIsAdded: Boolean = false,
    val showRateApp: Boolean = false,
)
