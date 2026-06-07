package fr.twentynine.keepon.ui.producer

import fr.twentynine.keepon.domain.model.ScreenTimeout
import fr.twentynine.keepon.domain.model.TimeoutIconStyle

/**
 * The remaining scalar inputs of the main UI state, grouped into one typed sub-combine
 * (combine is only typed up to 5 flows).
 */
internal data class MainPreferences(
    val resetTimeoutWhenScreenOff: Boolean,
    val currentScreenTimeout: ScreenTimeout,
    val keepOnIsActive: Boolean,
    val isFirstLaunch: Boolean,
    val timeoutIconStyle: TimeoutIconStyle,
)
