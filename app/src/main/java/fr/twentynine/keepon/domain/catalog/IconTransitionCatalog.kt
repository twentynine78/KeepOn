package fr.twentynine.keepon.domain.catalog

import fr.twentynine.keepon.domain.model.AffineTransition
import fr.twentynine.keepon.domain.model.FadingEdge
import fr.twentynine.keepon.domain.model.IconTransition
import fr.twentynine.keepon.domain.model.LayerTransform
import fr.twentynine.keepon.domain.model.MorphTransition
import fr.twentynine.keepon.domain.model.ParticleTransition
import fr.twentynine.keepon.domain.model.WarpTransition

/**
 * Catalog of the available icon-change transitions, as data (mirrors [IconFontFamilyCatalog]).
 *
 * To add a new animation: add an entry here (an [AffineTransition] or a [RenderedTransition]), then a
 * label + string in the Style screen. The tile renderer, the widget, the FAB and the selector all
 * consume the catalog, so nothing else changes. Persistence stores the entry [IconTransition.id];
 * [fromId] falls back to [default] for any unknown id.
 */
object IconTransitionCatalog {

    val liquidMorph = MorphTransition(
        id = "liquid_morph",
    )

    val particles = ParticleTransition(
        id = "particles",
    )

    val warp = WarpTransition(
        id = "warp",
    )

    val fadeThrough = AffineTransition(
        id = "fade_through",
        enterAway = LayerTransform(scaleX = 0.92f, scaleY = 0.92f, alpha = 0f),
        exitAway = LayerTransform(scaleX = 1.08f, scaleY = 1.08f, alpha = 0f),
    )

    val flip = AffineTransition(
        id = "flip",
        enterAway = LayerTransform(rotationX = -90f),
        exitAway = LayerTransform(rotationX = 90f),
        sequential = true,
    )

    val swipeDown = AffineTransition(
        id = "swipe_down",
        enterAway = LayerTransform(translationYFraction = -1f),
        exitAway = LayerTransform(translationYFraction = 1f),
        fadingEdge = FadingEdge.VERTICAL,
    )

    val all: List<IconTransition> =
        listOf(liquidMorph, particles, warp, fadeThrough, flip, swipeDown)

    val default: IconTransition = liquidMorph

    fun fromId(id: String): IconTransition = all.firstOrNull { it.id == id } ?: default
}
