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
    val fadingEdge: FadingEdge
}

/**
 * Rigid-layer transition: the [enterAway]/[exitAway] transforms describe the "fully absent" state of
 * the incoming and outgoing icon, and the motion is the interpolation between that and identity.
 */
data class AffineTransition(
    override val id: String,
    val enterAway: LayerTransform,
    val exitAway: LayerTransform,
    override val fadingEdge: FadingEdge = FadingEdge.NONE,
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
    override val fadingEdge: FadingEdge = FadingEdge.NONE,
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
    override val fadingEdge: FadingEdge = FadingEdge.NONE,
) : RenderedTransition

/**
 * Particle dissolve: the outgoing glyph disintegrates into particles that scatter and fade while the
 * incoming glyph reassembles from particles converging into place. [grain] is the particle size and
 * spacing, and [scatter] the peak travel distance — both as a fraction of the icon size.
 */
data class ParticleTransition(
    override val id: String,
    val grain: Float = 0.06f,
    val scatter: Float = 0.35f,
    override val fadingEdge: FadingEdge = FadingEdge.NONE,
) : RenderedTransition
