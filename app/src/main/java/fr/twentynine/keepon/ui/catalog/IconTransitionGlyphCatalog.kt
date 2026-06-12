package fr.twentynine.keepon.ui.catalog

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.rounded.Cyclone
import androidx.compose.material.icons.rounded.Flip
import androidx.compose.material.icons.rounded.Grain
import androidx.compose.material.icons.rounded.Theaters
import androidx.compose.material.icons.rounded.Waves
import androidx.compose.ui.graphics.vector.ImageVector
import fr.twentynine.keepon.domain.catalog.IconTransitionCatalog

/**
 * Presentation glyphs for the domain [IconTransitionCatalog] transitions, shown in the animation
 * type grid tiles. Kept in the UI layer (like [IconTransitionLabelCatalog]) so Compose icon types
 * stay out of the domain.
 */
object IconTransitionGlyphCatalog {
    private val glyphById: Map<String, ImageVector> = mapOf(
        IconTransitionCatalog.liquidMorph.id to Icons.Outlined.WaterDrop,
        IconTransitionCatalog.particles.id to Icons.Rounded.Grain,
        IconTransitionCatalog.warp.id to Icons.Rounded.Waves,
        IconTransitionCatalog.vortex.id to Icons.Rounded.Cyclone,
        IconTransitionCatalog.flip.id to Icons.Rounded.Flip,
        IconTransitionCatalog.swipeDown.id to Icons.Rounded.Theaters,
    )

    /** Falls back to the default transition's glyph, mirroring [IconTransitionCatalog.fromId]. */
    fun glyphFor(id: String): ImageVector =
        glyphById[id] ?: glyphById.getValue(IconTransitionCatalog.default.id)

    /**
     * Rotation (degrees) applied to the glyph wherever it is drawn: the M3 Flip icon mirrors
     * around a vertical axis while the flip animation tips horizontally, so it is turned 90°.
     */
    fun glyphRotationFor(id: String): Float =
        if (id == IconTransitionCatalog.flip.id) 90f else 0f
}
