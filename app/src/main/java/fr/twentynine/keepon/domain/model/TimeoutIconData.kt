package fr.twentynine.keepon.domain.model

import androidx.compose.runtime.Stable

@Stable
data class TimeoutIconData(
    val iconTimeout: ScreenTimeout,
    val iconSize: TimeoutIconSize,
    val iconStyle: TimeoutIconStyle
)
