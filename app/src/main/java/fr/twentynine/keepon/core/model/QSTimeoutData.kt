package fr.twentynine.keepon.core.model

import fr.twentynine.keepon.domain.model.TimeoutIconData

data class QSTimeoutData(
    val keepOnState: Boolean,
    val iconData: TimeoutIconData
)
