package fr.twentynine.keepon.core.transition.renderer

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.core.graphics.createBitmap
import fr.twentynine.keepon.core.transition.IconTransitionRenderer
import fr.twentynine.keepon.core.transition.TransitionFrames
import fr.twentynine.keepon.core.transition.util.ALPHA_OPAQUE
import fr.twentynine.keepon.core.transition.util.IconMask
import fr.twentynine.keepon.core.transition.util.ValueNoiseField
import fr.twentynine.keepon.domain.model.WarpTransition
import kotlin.math.cos
import kotlin.math.sin

/**
 * Turbulent dissolve: cross-fades the outgoing and incoming icon while pushing each through a
 * value-noise mesh, the displacement peaking at mid-transition and resolving to zero at both ends.
 * The displacement vectors counter-rotate as the transition advances, so the field evolves into an
 * actual swirl rather than a fixed push-and-return; the cross-fade is compressed to a narrow window
 * around the peak, so each glyph reads alone for most of its half and the two swap under cover of the
 * turbulence instead of lingering as a 50/50 double exposure.
 */
class WarpTransitionRenderer(private val transition: WarpTransition) : IconTransitionRenderer {

    override fun prepare(from: Bitmap, to: Bitmap): TransitionFrames {
        val softFrom = IconMask.toSoftwareMask(from)
        val softTo = IconMask.toSoftwareMask(to)
        val width = to.width
        val height = to.height
        val amplitudePx = transition.amplitude * maxOf(width, height)
        // One noise field per axis, fixed seeds for a stable swirl across frames.
        val noiseX = ValueNoiseField(SEED_X, NOISE_GRID)
        val noiseY = ValueNoiseField(SEED_Y, NOISE_GRID)
        // Mesh + paint reused across this playback's layers/frames (a prepared instance is never
        // composited concurrently); every draw fully rewrites the vertices before using them.
        val verts = FloatArray((MESH_CELLS + 1) * (MESH_CELLS + 1) * 2)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { isFilterBitmap = true }
        return TransitionFrames { progress ->
            val output = createBitmap(width, height, Bitmap.Config.ALPHA_8)
            val canvas = Canvas(output)
            val envelope = ENVELOPE_PEAK * progress * (1f - progress)
            val displacement = amplitudePx * envelope
            // The swap is compressed to a narrow window around the peak: each glyph holds alone for the
            // rest of its half. Keeping the two alphas complementary keeps the total coverage ~constant.
            val newAlpha = smoothstep(((progress - CROSS_START) / CROSS_SPAN).coerceIn(0f, 1f))
            // The field counter-rotates over the transition so the warp evolves into a swirl.
            val swirl = SWIRL_RADIANS * progress
            drawLayer(canvas, softFrom, width, height, displacement, swirl, noiseX, noiseY, 1f - newAlpha, verts, paint)
            drawLayer(canvas, softTo, width, height, -displacement, -swirl, noiseX, noiseY, newAlpha, verts, paint)
            output
        }
    }

    @Suppress("LongParameterList")
    private fun drawLayer(
        canvas: Canvas,
        bitmap: Bitmap,
        width: Int,
        height: Int,
        displacement: Float,
        swirl: Float,
        noiseX: ValueNoiseField,
        noiseY: ValueNoiseField,
        alpha: Float,
        verts: FloatArray,
        paint: Paint,
    ) {
        if (alpha <= 0f) return
        val cosSwirl = cos(swirl)
        val sinSwirl = sin(swirl)
        var i = 0
        for (row in 0..MESH_CELLS) {
            val v = row.toFloat() / MESH_CELLS
            for (col in 0..MESH_CELLS) {
                val u = col.toFloat() / MESH_CELLS
                val noiseDx = noiseX.sample(u, v)
                val noiseDy = noiseY.sample(u, v)
                verts[i++] = u * width + (noiseDx * cosSwirl - noiseDy * sinSwirl) * displacement
                verts[i++] = v * height + (noiseDx * sinSwirl + noiseDy * cosSwirl) * displacement
            }
        }
        paint.alpha = (alpha.coerceIn(0f, 1f) * ALPHA_OPAQUE).toInt()
        canvas.drawBitmapMesh(bitmap, MESH_CELLS, MESH_CELLS, verts, 0, null, 0, paint)
    }

    /** Cubic ease-in-out, used to soften the knees of the compressed cross-fade window. */
    private fun smoothstep(t: Float): Float = t * t * (3f - 2f * t)

    private companion object {
        const val MESH_CELLS = 10
        const val NOISE_GRID = 4
        const val SEED_X = 0x9E3779B9L
        const val SEED_Y = 0x85EBCA77L
        const val ENVELOPE_PEAK = 4f

        // Peak rotation (radians) the displacement field sweeps through over the transition.
        const val SWIRL_RADIANS = 1.2f

        // Cross-fade window: the swap happens over [CROSS_START, CROSS_START + CROSS_SPAN], centred on
        // the warp peak, instead of spanning the whole transition.
        const val CROSS_START = 0.35f
        const val CROSS_SPAN = 0.30f
    }
}
