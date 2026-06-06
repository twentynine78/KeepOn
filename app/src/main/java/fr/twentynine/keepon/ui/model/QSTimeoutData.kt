package fr.twentynine.keepon.ui.model

import androidx.compose.runtime.Stable
import fr.twentynine.keepon.domain.model.TimeoutIconData

@Stable
data class QSTimeoutData(
    val keepOnState: Boolean,
    val iconData: TimeoutIconData
)
