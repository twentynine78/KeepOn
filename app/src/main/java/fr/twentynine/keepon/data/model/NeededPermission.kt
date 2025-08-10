package fr.twentynine.keepon.data.model

data class NeededPermission(
    val title: String,
    val description: String,
    val requestNeeded: Boolean,
    val requestAction: () -> Unit,
)
