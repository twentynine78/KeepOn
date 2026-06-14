package fr.twentynine.keepon.ui.state

/**
 * The device/permission conditions a tip can be gated on. The tips catalog matches each tip's
 * required constraints against this snapshot to decide which tips are currently relevant (e.g. only
 * show the notification tip while [canPostNotification] is false).
 */
data class TipsConstraintState(
    val canPostNotification: Boolean = false,
    val tileServiceIsAdded: Boolean = false,
    val showRateApp: Boolean = false,
)
