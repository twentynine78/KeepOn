package fr.twentynine.keepon.domain.model

/**
 * A configurable icon-change transition, described purely as data. Every surface consumes the same
 * definition (the QS tile and widget render per-frame bitmaps, the FAB animates in Compose); the
 * Android-specific rendering lives in the `core/transition` renderers.
 *
 * [AffineTransition] is animated natively (in Compose on the FAB). Everything else is a
 * [RenderedTransition], composited per frame by the renderer from the two icon bitmaps.
 */
sealed interface IconTransition {
    val id: String
}

/**
 * Rigid-layer transition: the [enterAway]/[exitAway] transforms describe the "fully absent" state of
 * the incoming and outgoing icon, and the motion is the interpolation between that and identity.
 *
 * This is the general affine path — any translate / scale / `rotationX` / alpha layer motion,
 * animated natively by `graphicsLayer` on the FAB (vs the per-frame compositing of a
 * [RenderedTransition]). The catalog currently uses it only for `flip`, but it is the home for any
 * such rigid-transform effect, so it stays named for the mechanism rather than that one entry.
 */
data class AffineTransition(
    override val id: String,
    val enterAway: LayerTransform,
    val exitAway: LayerTransform,
    val sequential: Boolean = false,
) : IconTransition {
    /** Transform of one layer given its [presence] (0 = fully absent → 1 = fully in place). */
    fun transform(entering: Boolean, presence: Float): LayerTransform =
        LayerTransform.lerp(if (entering) enterAway else exitAway, LayerTransform.Identity, presence)

    /**
     * Outgoing + incoming transforms at [progress] (0 → 1). When [sequential] the outgoing layer
     * leaves over the first half and the incoming one arrives over the second half (used for the
     * flip, so the two never overlap); otherwise both cross simultaneously.
     */
    fun transformsAt(progress: Float): Pair<LayerTransform, LayerTransform> {
        val oldPresence: Float
        val newPresence: Float
        if (sequential) {
            oldPresence = 1f - (progress * 2f).coerceAtMost(1f)
            newPresence = (progress * 2f - 1f).coerceAtLeast(0f)
        } else {
            oldPresence = 1f - progress
            newPresence = progress
        }
        return transform(entering = false, presence = oldPresence) to
            transform(entering = true, presence = newPresence)
    }
}

/**
 * A transition the renderer composites per frame from the outgoing and incoming icon bitmaps
 * (the tile/widget push the resulting bitmaps, the FAB plays them as a frame sequence).
 */
sealed interface RenderedTransition : IconTransition

/**
 * Silhouette metamorphosis: the renderer interpolates a signed distance field of each glyph and
 * thresholds it, so the strokes of the outgoing icon flow and reconnect into the incoming one.
 * [edgeSoftness] is the half-width (in pixels) of the smooth band that anti-aliases the morphed
 * contour. The motion adapts to any pair of glyphs with no per-pair tuning.
 */
data class MorphTransition(
    override val id: String,
    val edgeSoftness: Float = 1.5f,
) : RenderedTransition

/**
 * Turbulent dissolve: the outgoing and incoming icons cross-fade while a noise field warps them,
 * the displacement peaking at mid-transition and resolving to zero at both ends, so the glyphs swirl
 * into one another. [amplitude] is the peak displacement as a fraction of the icon size. Reads well
 * even when the two glyphs are very different, since it dissolves rather than forcing a shape match.
 */
data class WarpTransition(
    override val id: String,
    val amplitude: Float = 0.05f,
) : RenderedTransition

/**
 * Particle dissolve: the outgoing glyph disintegrates into particles that scatter and fade while the
 * incoming glyph reassembles from particles converging into place. [grain] is the particle size and
 * spacing, and [scatter] the peak travel distance — both as a fraction of the icon size.
 */
data class ParticleTransition(
    override val id: String,
    val grain: Float = 0.06f,
    val scatter: Float = 0.55f,
) : RenderedTransition

/**
 * Vortex drain: the outgoing glyph is sucked into the centre along a spiral (its mesh collapses to a
 * point while twisting, more sharply near the core) and vanishes, then the incoming glyph re-emerges
 * from the centre and unwinds back into its shape — the same motion played in reverse. [twist] is the
 * peak spiral rotation in radians (the centre sweeps through it; the rim barely turns).
 */
data class VortexTransition(
    override val id: String,
    val twist: Float = 5.65f,
) : RenderedTransition

/**
 * Cylindrical reel: the two icons are glued to a vertical rotating drum at adjacent notches; on a
 * change the drum rolls one notch. The outgoing glyph scrolls down and foreshortens as it wraps off
 * the bottom edge while the incoming one emerges curved from the top and unrolls flat into place —
 * a combination-padlock wheel / slot-machine reel, with no alpha fade (appearance is purely the sine
 * foreshortening and the clipping at the box edges). [arcDegrees] is one notch's arc on the drum:
 * larger reads as a tighter, more dramatic wheel. A per-layer roll envelope flattens each glyph to a
 * rigid slab as it reaches the front, so the settled frame is the crisp icon.
 */
data class ReelTransition(
    override val id: String,
    val arcDegrees: Float = 105f,
    /** Camera distance as a multiple of the drum radius; smaller = stronger 3D bulge. Must be > 1. */
    val camera: Float = 2.2f,
) : RenderedTransition
