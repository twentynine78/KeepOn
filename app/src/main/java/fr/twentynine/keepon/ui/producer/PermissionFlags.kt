package fr.twentynine.keepon.ui.producer

/**
 * The three runtime-grantable permission flags, grouped so the state producers can fold them into
 * a single typed sub-combine (combine is only typed up to 5 flows). Shared by the main and Tasker
 * state producers.
 */
internal data class PermissionFlags(
    val canWriteSystemSettings: Boolean,
    val batteryIsNotOptimized: Boolean,
    val canPostNotification: Boolean,
)
