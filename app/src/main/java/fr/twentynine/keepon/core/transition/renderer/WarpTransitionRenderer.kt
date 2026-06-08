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

/**
 * Turbulent dissolve: cross-fades the outgoing and incoming icon while pushing each through a
 * value-noise mesh in opposite directions, the displacement peaking at mid-transition and resolving
 * to zero at both ends, so the glyphs swirl into one another.
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
            drawLayer(canvas, softFrom, width, height, displacement, noiseX, noiseY, 1f - progress, verts, paint)
            drawLayer(canvas, softTo, width, height, -displacement, noiseX, noiseY, progress, verts, paint)
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
        noiseX: ValueNoiseField,
        noiseY: ValueNoiseField,
        alpha: Float,
        verts: FloatArray,
        paint: Paint,
    ) {
        if (alpha <= 0f) return
        var i = 0
        for (row in 0..MESH_CELLS) {
            val v = row.toFloat() / MESH_CELLS
            for (col in 0..MESH_CELLS) {
                val u = col.toFloat() / MESH_CELLS
                verts[i++] = u * width + noiseX.sample(u, v) * displacement
                verts[i++] = v * height + noiseY.sample(u, v) * displacement
            }
        }
        paint.alpha = (alpha.coerceIn(0f, 1f) * ALPHA_OPAQUE).toInt()
        canvas.drawBitmapMesh(bitmap, MESH_CELLS, MESH_CELLS, verts, 0, null, 0, paint)
    }

    private companion object {
        const val MESH_CELLS = 10
        const val NOISE_GRID = 4
        const val SEED_X = 0x9E3779B9L
        const val SEED_Y = 0x85EBCA77L
        const val ENVELOPE_PEAK = 4f
    }
}
