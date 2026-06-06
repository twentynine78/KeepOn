package fr.twentynine.keepon.ui.model

import androidx.compose.runtime.Immutable

@Immutable
data class NeededPermission(
    val title: String,
    val description: String,
    val requestNeeded: Boolean,
    val requestAction: () -> Unit,
)
