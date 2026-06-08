package fr.twentynine.keepon.core.transition.renderer

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.core.graphics.createBitmap
import fr.twentynine.keepon.core.transition.IconTransitionRenderer
import fr.twentynine.keepon.core.transition.TransitionFrames
import fr.twentynine.keepon.core.transition.util.ALPHA_OPAQUE
import fr.twentynine.keepon.core.transition.util.IconMask
import fr.twentynine.keepon.domain.model.ReelTransition
import kotlin.math.sin

private val DEGREES_TO_RADIANS = (Math.PI / 180.0).toFloat()

/**
 * Cylindrical reel: rolls the drum one notch so the outgoing glyph wraps off the bottom while the
 * incoming one unrolls in from the top. Each layer is pushed through a [Canvas.drawBitmapMesh] grid
 * whose rows are foreshortened by the sine of their angle on the drum (columns stay linear — a
 * vertical-axis cylinder has no horizontal narrowing), so a layer near the rim is squashed and curved
 * and a layer at the front fills the box flat. A per-layer roll envelope eases both the notch travel
 * and the curvature to zero as a layer reaches the front, so the incoming layer lands on the identity
 * mesh and the last frame is the crisp glyph. Both layers are opaque — appearance and disappearance
 * are purely the foreshortening plus the clipping at the box edges, with no alpha fade.
 */
class ReelTransitionRenderer(private val transition: ReelTransition) : IconTransitionRenderer {

    override fun prepare(from: Bitmap, to: Bitmap): TransitionFrames {
        val softFrom = IconMask.toSoftwareMask(from)
        val softTo = IconMask.toSoftwareMask(to)
        val width = to.width
        val height = to.height
        // One notch's arc and the drum radius (in icon-height units) that makes the front glyph,
        // spanning [-arc/2, +arc/2], fill the height exactly.
        val arc = transition.arcDegrees * DEGREES_TO_RADIANS
        val radius = 0.5f / sin(arc / 2f)
        // Mesh + paint reused across this playback's layers/frames (a prepared instance is never
        // composited concurrently); every draw fully rewrites the vertices before using them.
        val verts = FloatArray((MESH_COLS + 1) * (MESH_ROWS + 1) * 2)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { isFilterBitmap = true }
        return TransitionFrames { progress ->
            val output = createBitmap(width, height, Bitmap.Config.ALPHA_8)
            val canvas = Canvas(output)
            // Each layer's roll: 1 = fully wrapped onto the rim, 0 = settled flat at the front. The
            // incoming layer settles at the end (roll 1 at progress 1), the outgoing one at the start.
            val rollIn = smoothstep(progress)
            val rollOut = smoothstep(1f - progress)
            // Outgoing rolls down and off the bottom (negative notch); incoming unrolls in from the top.
            drawLayer(canvas, softFrom, width, height, arc, radius, -1f, rollOut, verts, paint)
            drawLayer(canvas, softTo, width, height, arc, radius, 1f, rollIn, verts, paint)
            output
        }
    }

    @Suppress("LongParameterList")
    private fun drawLayer(
        canvas: Canvas,
        bitmap: Bitmap,
        width: Int,
        height: Int,
        arc: Float,
        radius: Float,
        notchSign: Float,
        roll: Float,
        verts: FloatArray,
        paint: Paint,
    ) {
        // Centre angle: one notch (plus a small empty gap between glyphs) away from the front, eased to
        // the front by the roll. Curvature is full mid-roll and flattens to a rigid slab at rest, so the
        // settled end (roll 0) is an exact flat full-height slab — the crisp icon.
        val centerAngle = notchSign * arc * (1f + GAP_FRACTION) * (1f - roll)
        val curve = 1f - roll
        val yCenter = -radius * sin(centerAngle)
        var i = 0
        for (row in 0..MESH_ROWS) {
            val v = row.toFloat() / MESH_ROWS
            // The row's angle on the drum and its sine-foreshortened height, blended toward a flat slab
            // by the curve factor. x stays linear (vertical-axis cylinder = no horizontal narrowing).
            val phi = centerAngle + (0.5f - v) * arc
            val yCyl = -radius * sin(phi)
            val yFlat = yCenter + (v - 0.5f)
            val screenY = (0.5f + yFlat + (yCyl - yFlat) * curve) * height
            for (col in 0..MESH_COLS) {
                verts[i++] = (col.toFloat() / MESH_COLS) * width
                verts[i++] = screenY
            }
        }
        paint.alpha = ALPHA_OPAQUE.toInt()
        canvas.drawBitmapMesh(bitmap, MESH_COLS, MESH_ROWS, verts, 0, null, 0, paint)
    }

    /** Cubic ease-in-out, shaping each layer's roll so it eases off the rim and settles flat. */
    private fun smoothstep(t: Float): Float = t * t * (3f - 2f * t)

    private companion object {
        // The curve is vertical-only: rows carry the sine foreshortening (need to be smooth), columns
        // are linear (two is the minimum that still maps the rectangle).
        const val MESH_ROWS = 24
        const val MESH_COLS = 2

        // Empty sliver between the two notches so the abutting layer edges never double-darken at the
        // seam; folded into the notch travel and eased out with the roll, so it never offsets the
        // settled icon.
        const val GAP_FRACTION = 0.04f
    }
}
