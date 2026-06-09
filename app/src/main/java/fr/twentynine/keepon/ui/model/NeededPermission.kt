package fr.twentynine.keepon.ui.model

import androidx.compose.runtime.Immutable

/**
 * One permission row on the permission-onboarding screen: its [title]/[description], whether it still
 * [requestNeeded]s granting, and the [requestAction] that launches its system prompt.
 */
@Immutable
data class NeededPermission(
    val title: String,
    val description: String,
    val requestNeeded: Boolean,
    val requestAction: () -> Unit,
)
