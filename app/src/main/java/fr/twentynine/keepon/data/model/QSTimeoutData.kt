package fr.twentynine.keepon.data.model

import androidx.compose.runtime.Stable

@Stable
data class QSTimeoutData(
    val keepOnState: Boolean,
    val iconData: TimeoutIconData
)
