package fr.twentynine.keepon.domain.catalog

import fr.twentynine.keepon.domain.model.AffineTransition
import fr.twentynine.keepon.domain.model.IconTransition
import fr.twentynine.keepon.domain.model.LayerTransform
import fr.twentynine.keepon.domain.model.MorphTransition
import fr.twentynine.keepon.domain.model.ParticleTransition
import fr.twentynine.keepon.domain.model.ReelTransition
import fr.twentynine.keepon.domain.model.VortexTransition
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
        amplitude = 0.12f, // ~2.4× la valeur par défaut : distorsion bien visible, glyphe encore lisible
    )

    val vortex = VortexTransition(
        id = "vortex",
    )

    val flip = AffineTransition(
        id = "flip",
        enterAway = LayerTransform(rotationX = -90f),
        exitAway = LayerTransform(rotationX = 90f),
        sequential = true,
    )

    // Rouleau cylindrique (molette de cadenas / bandeau de machine à sous) ; id "swipe_down" conservé
    // pour les préférences existantes.
    val swipeDown = ReelTransition(
        id = "swipe_down",
    )

    val all: List<IconTransition> =
        listOf(liquidMorph, particles, warp, vortex, flip, swipeDown)

    val default: IconTransition = liquidMorph

    fun fromId(id: String): IconTransition = all.firstOrNull { it.id == id } ?: default
}
