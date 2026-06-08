package fr.twentynine.keepon.core.transition.renderer

import android.graphics.Bitmap
import androidx.core.graphics.createBitmap
import fr.twentynine.keepon.core.transition.IconTransitionRenderer
import fr.twentynine.keepon.core.transition.TransitionFrames
import fr.twentynine.keepon.core.transition.util.ALPHA_OPAQUE
import fr.twentynine.keepon.core.transition.util.IconMask
import fr.twentynine.keepon.core.transition.util.SignedDistanceField
import fr.twentynine.keepon.domain.model.MorphTransition
import java.nio.ByteBuffer

private const val MIN_SOFTNESS = 0.001f

/**
 * Silhouette metamorphosis: interpolate the two glyphs' signed distance fields at the progress and
 * threshold the result at the contour (field = 0), with a [MorphTransition.edgeSoftness]-wide
 * smoothstep band for anti-aliasing, so the strokes flow into one another.
 */
class MorphTransitionRenderer(private val transition: MorphTransition) : IconTransitionRenderer {

    override fun prepare(from: Bitmap, to: Bitmap): TransitionFrames {
        val width = to.width
        val height = to.height
        val sdfFrom = SignedDistanceField.of(IconMask.toAlphaMask(from, width, height))
        val sdfTo = SignedDistanceField.of(IconMask.toAlphaMask(to, width, height))
        val softness = transition.edgeSoftness.coerceAtLeast(MIN_SOFTNESS)
        // Row buffer reused across this playback's frames (a prepared instance is never composited
        // concurrently), so the per-frame ALPHA_8 fill allocates once instead of once per frame.
        var scratch: ByteArray? = null
        return TransitionFrames { progress ->
            val output = createBitmap(width, height, Bitmap.Config.ALPHA_8)
            val rowBytes = output.rowBytes
            val bytes = scratch ?: ByteArray(rowBytes * height).also { scratch = it }
            for (y in 0 until height) {
                val rowBase = y * rowBytes
                val srcBase = y * width
                for (x in 0 until width) {
                    val i = srcBase + x
                    val field = sdfFrom[i] + (sdfTo[i] - sdfFrom[i]) * progress
                    bytes[rowBase + x] = (contourCoverage(field, softness) * ALPHA_OPAQUE).toInt().toByte()
                }
            }
            output.copyPixelsFromBuffer(ByteBuffer.wrap(bytes))
            output
        }
    }

    /** Smoothstepped silhouette coverage: 1 inside the contour (field < 0), 0 outside, AA over a band. */
    private fun contourCoverage(field: Float, softness: Float): Float {
        val edge = ((softness - field) / (2f * softness)).coerceIn(0f, 1f)
        return edge * edge * (3f - 2f * edge)
    }
}
