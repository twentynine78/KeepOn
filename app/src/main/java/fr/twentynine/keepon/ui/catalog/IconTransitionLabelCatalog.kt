package fr.twentynine.keepon.ui.catalog

import fr.twentynine.keepon.R
import fr.twentynine.keepon.domain.catalog.IconTransitionCatalog

/**
 * Localized presentation labels for the domain [IconTransitionCatalog] transitions. Kept in the UI
 * layer (like [TipsInfo]) so the resource ids stay out of the domain; the label text is resolved
 * through the StringResourceProvider where the list is built.
 */
object IconTransitionLabelCatalog {
    private val labelResById: Map<String, Int> = mapOf(
        IconTransitionCatalog.liquidMorph.id to R.string.icon_transition_type_liquid_morph,
        IconTransitionCatalog.particles.id to R.string.icon_transition_type_particles,
        IconTransitionCatalog.warp.id to R.string.icon_transition_type_warp,
        IconTransitionCatalog.vortex.id to R.string.icon_transition_type_vortex,
        IconTransitionCatalog.flip.id to R.string.icon_transition_type_flip,
        IconTransitionCatalog.swipeDown.id to R.string.icon_transition_type_swipe_down,
    )

    /** Falls back to the default transition's label, mirroring [IconTransitionCatalog.fromId]. */
    fun labelResFor(id: String): Int =
        labelResById[id] ?: labelResById.getValue(IconTransitionCatalog.default.id)
}
