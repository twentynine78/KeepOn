package fr.twentynine.keepon.domain.model

import fr.twentynine.keepon.domain.catalog.IconTransitionCatalog
import kotlinx.serialization.Serializable

/**
 * User configuration of the icon-change transition: whether it plays at all and which animation to
 * use. Disabled by default. [typeId] references an [IconTransition.id] from the catalog (stored as
 * a stable string, like the icon font is stored by name).
 */
@Serializable
data class IconTransitionAnimation(
    val enabled: Boolean = false,
    val typeId: String = IconTransitionCatalog.default.id,
    /** Slider notch for the animation duration (0 = default, see [IconTransitionTiming.durationMs]). */
    val durationStep: Int = 0,
)
