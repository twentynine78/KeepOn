package fr.twentynine.keepon.data.model

import androidx.compose.runtime.Stable
import fr.twentynine.keepon.data.enums.TimeoutIconSize

@Stable
data class TimeoutIconData(
    val iconTimeout: ScreenTimeout,
    val iconSize: TimeoutIconSize,
    val iconStyle: TimeoutIconStyle
)
