package fr.twentynine.keepon.data.model

import androidx.compose.runtime.Immutable

@Immutable
data class NeededPermission(
    val title: String,
    val description: String,
    val requestNeeded: Boolean,
    val requestAction: () -> Unit,
)
