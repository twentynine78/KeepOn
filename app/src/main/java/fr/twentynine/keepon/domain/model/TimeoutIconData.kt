package fr.twentynine.keepon.domain.model

/**
 * Everything needed to render one timeout icon: which [iconTimeout] to show, at what [iconSize], in
 * which [iconStyle]. Used as the Coil cache key for generated icons, so two requests with equal data
 * resolve to the same cached bitmap.
 */
data class TimeoutIconData(
    val iconTimeout: ScreenTimeout,
    val iconSize: TimeoutIconSize,
    val iconStyle: TimeoutIconStyle
)
