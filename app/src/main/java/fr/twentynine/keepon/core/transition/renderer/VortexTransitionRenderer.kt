package fr.twentynine.keepon.core.transition.renderer

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.core.graphics.createBitmap
import fr.twentynine.keepon.core.transition.IconTransitionRenderer
import fr.twentynine.keepon.core.transition.TransitionFrames
import fr.twentynine.keepon.core.transition.util.ALPHA_OPAQUE
import fr.twentynine.keepon.core.transition.util.IconMask
import fr.twentynine.keepon.domain.model.VortexTransition
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

/**
 * Vortex drain: pulls the outgoing icon into the centre along a spiral and lets the incoming one
 * re-emerge from it. Each layer is pushed through a [Canvas.drawBitmapMesh] grid whose vertices are
 * rotated about the centre by an angle that grows towards the core (the rim barely turns, the centre
 * sweeps the full [VortexTransition.twist]) and scaled radially towards / away from the centre. The
 * two halves are sequential — the outgoing glyph collapses to a point over the first half, the
 * incoming one expands back out over the second — with a small overlap so the pinch is never a fully
 * empty frame. The incoming half lands on the identity mesh, so the last frame is the crisp glyph.
 */
class VortexTransitionRenderer(private val transition: VortexTransition) : IconTransitionRenderer {

    override fun prepare(from: Bitmap, to: Bitmap): TransitionFrames {
        val softFrom = IconMask.toSoftwareMask(from)
        val softTo = IconMask.toSoftwareMask(to)
        val width = to.width
        val height = to.height
        // Mesh + paint reused across this playback's layers/frames (a prepared instance is never
        // composited concurrently); every draw fully rewrites the vertices before using them.
        val verts = FloatArray((MESH_CELLS + 1) * (MESH_CELLS + 1) * 2)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { isFilterBitmap = true }
        return TransitionFrames { progress ->
            val output = createBitmap(width, height, Bitmap.Config.ALPHA_8)
            val canvas = Canvas(output)
            // Collapse runs over the first half (+overlap), emerge over the second half (−overlap), so
            // the two cross briefly around the pinch instead of leaving a blank frame at progress 0.5.
            val collapse = (progress / COLLAPSE_END).coerceIn(0f, 1f)
            val emerge = ((progress - EMERGE_START) / (1f - EMERGE_START)).coerceIn(0f, 1f)
            // Outgoing: shrinks to a point while winding up, fading out as it is swallowed.
            drawLayer(canvas, softFrom, width, height, 1f - collapse, transition.twist * collapse, 1f - smoothstep(collapse), verts, paint)
            // Incoming: grows back from the point while unwinding the (opposite) twist, fading in.
            drawLayer(canvas, softTo, width, height, emerge, -transition.twist * (1f - emerge), smoothstep(emerge), verts, paint)
            output
        }
    }

    @Suppress("LongParameterList")
    private fun drawLayer(
        canvas: Canvas,
        bitmap: Bitmap,
        width: Int,
        height: Int,
        scale: Float,
        twist: Float,
        alpha: Float,
        verts: FloatArray,
        paint: Paint,
    ) {
        if (alpha <= 0f || scale <= 0f) return
        var i = 0
        for (row in 0..MESH_CELLS) {
            val dy = row.toFloat() / MESH_CELLS - 0.5f
            for (col in 0..MESH_CELLS) {
                val dx = col.toFloat() / MESH_CELLS - 0.5f
                // Rotate more towards the centre (rNorm 0) and barely at all at the rim (rNorm 1).
                val theta = twist * (1f - (hypot(dx, dy) / MAX_RADIUS).coerceAtMost(1f))
                val cosT = cos(theta)
                val sinT = sin(theta)
                val rx = dx * cosT - dy * sinT
                val ry = dx * sinT + dy * cosT
                verts[i++] = (0.5f + rx * scale) * width
                verts[i++] = (0.5f + ry * scale) * height
            }
        }
        paint.alpha = (alpha.coerceIn(0f, 1f) * ALPHA_OPAQUE).toInt()
        canvas.drawBitmapMesh(bitmap, MESH_CELLS, MESH_CELLS, verts, 0, null, 0, paint)
    }

    /** Cubic ease-in-out, softening the fade knees at each half. */
    private fun smoothstep(t: Float): Float = t * t * (3f - 2f * t)

    private companion object {
        const val MESH_CELLS = 16

        // Half overlap each phase steals from the midpoint, so the pinch never blanks a whole frame.
        const val OVERLAP = 0.05f
        const val COLLAPSE_END = 0.5f + OVERLAP
        const val EMERGE_START = 0.5f - OVERLAP

        // Distance from centre to a corner of the unit square, normalising the per-vertex radius.
        const val MAX_RADIUS = 0.70710677f
    }
}
