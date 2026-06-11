package fr.twentynine.keepon.core.transition.renderer

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import androidx.core.graphics.createBitmap
import fr.twentynine.keepon.core.transition.IconTransitionRenderer
import fr.twentynine.keepon.core.transition.TransitionFrames
import fr.twentynine.keepon.core.transition.util.ALPHA_OPAQUE
import fr.twentynine.keepon.core.transition.util.ALPHA_THRESHOLD_FRACTION
import fr.twentynine.keepon.core.transition.util.IconMask
import fr.twentynine.keepon.core.transition.util.SignedDistanceField
import fr.twentynine.keepon.core.transition.util.smoothstep
import fr.twentynine.keepon.domain.model.MorphTransition
import java.nio.ByteBuffer

private const val MIN_SOFTNESS = 0.001f

// Floor for the alignment scale: keeps it positive (it is inverted for the placement transform).
private const val MIN_ALIGNMENT_SCALE = 0.01f

/**
 * Silhouette metamorphosis: interpolate the two glyphs' signed distance fields at the progress and
 * threshold the result at the contour (field = 0), with a [MorphTransition.edgeSoftness]-wide
 * smoothstep band for anti-aliasing, so the strokes flow into one another.
 *
 * The incoming glyph is first pre-aligned onto the outgoing one (its alpha-weighted centroid and ink
 * height matched), so the silhouettes are co-located and co-scaled before the field is blended. The
 * morph then reconnects the strokes *in place* rather than letting the zero-crossing slide across the
 * canvas when the two glyphs differ in position or height (e.g. "1" → "30"). A placement transform
 * interpolated from identity to the inverse alignment carries the morphed silhouette back to the
 * incoming glyph's true position and scale, so the last frame matches the crisp icon the surface
 * settles on.
 */
class MorphTransitionRenderer(private val transition: MorphTransition) : IconTransitionRenderer {

    override fun prepare(from: Bitmap, to: Bitmap): TransitionFrames {
        val width = to.width
        val height = to.height
        val fromMask = IconMask.toAlphaMask(from, width, height)
        val toMask = IconMask.toAlphaMask(to, width, height)
        val fromMetrics = metricsOf(IconMask.readAlpha(fromMask), width, height)
        val toMetrics = metricsOf(IconMask.readAlpha(toMask), width, height)

        // Align the incoming glyph onto the outgoing one (centroid + ink height) so the blended field
        // morphs in place; `scale` maps the incoming ink height onto the outgoing one.
        val scale = alignmentScale(fromMetrics, toMetrics, width, height)
        val sdfFrom = SignedDistanceField.of(fromMask)
        val sdfTo = SignedDistanceField.of(alignedMask(toMask, fromMetrics, toMetrics, scale))
        val softness = transition.edgeSoftness.coerceAtLeast(MIN_SOFTNESS)

        // Inverse of that alignment, interpolated from identity (progress 0) to itself (progress 1), so
        // the morphed silhouette ends at the incoming glyph's real position/scale (the crisp settle).
        val inverseScale = 1f / scale
        val translateX = toMetrics.cx - inverseScale * fromMetrics.cx
        val translateY = toMetrics.cy - inverseScale * fromMetrics.cy

        // Reused across this playback's frames (a prepared instance is never composited concurrently);
        // only the freshly-emitted output bitmap is allocated per frame.
        val morphBitmap = createBitmap(width, height, Bitmap.Config.ALPHA_8)
        val scratch = ByteArray(morphBitmap.rowBytes * height)
        val placePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { isFilterBitmap = true }

        return TransitionFrames { progress ->
            fillSilhouette(morphBitmap, scratch, sdfFrom, sdfTo, softness, progress)
            placeSilhouette(morphBitmap, inverseScale, translateX, translateY, placePaint, progress)
        }
    }

    /**
     * The alignment scale: the incoming ink height mapped onto the outgoing one, capped so the
     * aligned ink's bounding box stays inside the canvas. The aligned mask is rendered on the
     * icon-sized canvas, so an unbounded upscale (outgoing ink much taller than the incoming one,
     * e.g. a full-height tile glyph morphing into a wide short-text timeout icon) would clip the
     * incoming glyph at the canvas edges — visible as an oversized icon with its sides cut off.
     */
    private fun alignmentScale(from: GlyphMetrics, to: GlyphMetrics, width: Int, height: Int): Float {
        // The alignment maps x to (x - to.cx) * scale + from.cx (same vertically): each ink bounding
        // box edge yields the largest scale that keeps it within [0, canvas].
        var scale = from.inkHeight / to.inkHeight
        if (to.cx > to.minX) scale = minOf(scale, from.cx / (to.cx - to.minX))
        if (to.maxX > to.cx) scale = minOf(scale, (width - from.cx) / (to.maxX - to.cx))
        if (to.cy > to.minY) scale = minOf(scale, from.cy / (to.cy - to.minY))
        if (to.maxY > to.cy) scale = minOf(scale, (height - from.cy) / (to.maxY - to.cy))
        return scale.coerceAtLeast(MIN_ALIGNMENT_SCALE)
    }

    /** Renders the incoming mask pre-aligned onto the outgoing glyph (centroid matched, ink height scaled). */
    private fun alignedMask(toMask: Bitmap, from: GlyphMetrics, to: GlyphMetrics, scale: Float): Bitmap {
        val aligned = createBitmap(toMask.width, toMask.height, Bitmap.Config.ALPHA_8)
        Canvas(aligned).drawBitmap(
            toMask,
            Matrix().apply {
                postTranslate(-to.cx, -to.cy)
                postScale(scale, scale)
                postTranslate(from.cx, from.cy)
            },
            Paint(Paint.ANTI_ALIAS_FLAG).apply { isFilterBitmap = true },
        )
        return aligned
    }

    /** Thresholds the blended field at [progress] into [target]'s alpha (via the reused [scratch] row buffer). */
    private fun fillSilhouette(
        target: Bitmap,
        scratch: ByteArray,
        sdfFrom: FloatArray,
        sdfTo: FloatArray,
        softness: Float,
        progress: Float,
    ) {
        val width = target.width
        val height = target.height
        val rowBytes = target.rowBytes
        for (y in 0 until height) {
            val rowBase = y * rowBytes
            val srcBase = y * width
            for (x in 0 until width) {
                val i = srcBase + x
                val field = sdfFrom[i] + (sdfTo[i] - sdfFrom[i]) * progress
                scratch[rowBase + x] = (contourCoverage(field, softness) * ALPHA_OPAQUE).toInt().toByte()
            }
        }
        target.copyPixelsFromBuffer(ByteBuffer.wrap(scratch))
    }

    /** Draws the [silhouette] into a fresh frame through the identity→inverse-alignment transform at [progress]. */
    private fun placeSilhouette(
        silhouette: Bitmap,
        inverseScale: Float,
        translateX: Float,
        translateY: Float,
        paint: Paint,
        progress: Float,
    ): Bitmap {
        val output = createBitmap(silhouette.width, silhouette.height, Bitmap.Config.ALPHA_8)
        val placeScale = 1f + (inverseScale - 1f) * progress
        val matrix = Matrix().apply {
            setScale(placeScale, placeScale)
            postTranslate(translateX * progress, translateY * progress)
        }
        Canvas(output).drawBitmap(silhouette, matrix, paint)
        return output
    }

    /** Smoothstepped silhouette coverage: 1 inside the contour (field < 0), 0 outside, AA over a band. */
    private fun contourCoverage(field: Float, softness: Float): Float {
        val edge = ((softness - field) / (2f * softness)).coerceIn(0f, 1f)
        return smoothstep(edge)
    }

    /** Alpha-weighted centroid and ink bounding box of the above-threshold pixels. */
    private fun metricsOf(alpha: FloatArray, width: Int, height: Int): GlyphMetrics {
        var mass = 0f
        var sumX = 0f
        var sumY = 0f
        var minX = width
        var maxX = -1
        var minY = height
        var maxY = -1
        for (y in 0 until height) {
            val rowBase = y * width
            for (x in 0 until width) {
                val a = alpha[rowBase + x]
                if (a > ALPHA_THRESHOLD_FRACTION) {
                    mass += a
                    sumX += a * x
                    sumY += a * y
                    if (x < minX) minX = x
                    if (x > maxX) maxX = x
                    if (y < minY) minY = y
                    if (y > maxY) maxY = y
                }
            }
        }
        if (mass <= 0f) {
            return GlyphMetrics(width / 2f, height / 2f, 0f, (width - 1).toFloat(), 0f, (height - 1).toFloat())
        }
        return GlyphMetrics(sumX / mass, sumY / mass, minX.toFloat(), maxX.toFloat(), minY.toFloat(), maxY.toFloat())
    }

    private class GlyphMetrics(
        val cx: Float,
        val cy: Float,
        val minX: Float,
        val maxX: Float,
        val minY: Float,
        val maxY: Float,
    ) {
        val inkHeight: Float get() = (maxY - minY + 1f).coerceAtLeast(1f)
    }
}
