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
import kotlin.math.cos
import kotlin.math.sin

private val DEGREES_TO_RADIANS = (Math.PI / 180.0).toFloat()

/**
 * Cylindrical reel: rolls the drum one notch so the outgoing glyph wraps off the bottom while the
 * incoming one unrolls in from the top. Each layer is pushed through a [Canvas.drawBitmapMesh] grid
 * under a one-point perspective camera in front of the drum: a row at drum angle phi sits at depth
 * R*cos(phi), so front rows project larger and wider and receding top/bottom rows smaller and
 * narrower — a real bulge, not a flat sine squash. Rows past the +/-90deg tangents are on the back of
 * the drum and are pushed hard off the box so they never re-emerge. A per-layer roll envelope eases
 * the notch travel and collapses both the curvature and the perspective to identity as a layer
 * reaches the front, so the settled frame is the crisp glyph. Both layers are opaque — appearance is
 * purely the projection plus the clipping at the box edges, with no alpha fade.
 */
class ReelTransitionRenderer(private val transition: ReelTransition) : IconTransitionRenderer {

    override fun prepare(from: Bitmap, to: Bitmap): TransitionFrames {
        val softFrom = IconMask.toSoftwareMask(from)
        val softTo = IconMask.toSoftwareMask(to)
        val width = to.width
        val height = to.height
        // One notch's arc and the drum radius (in icon-height units) that makes the flat front glyph,
        // spanning [-arc/2, +arc/2], fill the height exactly.
        val arc = transition.arcDegrees * DEGREES_TO_RADIANS
        val radius = 0.5f / sin(arc / 2f)
        // Camera distance in front of the drum centre (multiple of the radius). > radius always, so the
        // perspective denominator camera - radius*cos(phi) >= camera - radius > 0 — no singularity.
        val camera = radius * transition.camera.coerceAtLeast(MIN_CAMERA)
        // Front-centre perspective scale (nearest, largest) and the vertical normaliser that keeps the
        // fully curved+projected front glyph exactly one box tall.
        val frontScale = camera / (camera - radius)
        val edgeScale = camera / (camera - radius * cos(arc / 2f))
        val vNorm = 0.5f / (edgeScale * radius * sin(arc / 2f))
        // Mesh + paint reused across this playback's layers/frames (a prepared instance is never
        // composited concurrently); every draw fully rewrites the vertices before using them.
        val verts = FloatArray((MESH_COLS + 1) * (MESH_ROWS + 1) * 2)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { isFilterBitmap = true }
        return TransitionFrames { progress ->
            val output = createBitmap(width, height, Bitmap.Config.ALPHA_8)
            val canvas = Canvas(output)
            // Each layer's roll: 1 = settled flat at the front, 0 = fully wrapped onto the rim. The
            // incoming layer settles at the end (roll 1 at progress 1), the outgoing one at the start.
            val rollIn = smoothstep(progress)
            val rollOut = smoothstep(1f - progress)
            // Outgoing rolls down and off the bottom (negative notch); incoming unrolls in from the top.
            drawLayer(canvas, softFrom, width, height, arc, radius, camera, frontScale, vNorm, -1f, rollOut, verts, paint)
            drawLayer(canvas, softTo, width, height, arc, radius, camera, frontScale, vNorm, 1f, rollIn, verts, paint)
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
        camera: Float,
        frontScale: Float,
        vNorm: Float,
        notchSign: Float,
        roll: Float,
        verts: FloatArray,
        paint: Paint,
    ) {
        // Centre angle: one notch (plus a small empty gap between glyphs) away from the front, eased to
        // the front by the roll. cv is the curvature+perspective amount: full mid-roll, collapsing to a
        // flat identity slab at rest (roll 1), so the settled end is the crisp icon.
        val centerAngle = notchSign * arc * (1f + GAP_FRACTION) * (1f - roll)
        val cv = 1f - roll
        // Projected centre of the flat reference slab (clamped to the front face).
        val centerClamped = centerAngle.coerceIn(-HALF_PI, HALF_PI)
        val yCenter = 0.5f - perspScale(centerClamped, radius, camera) * radius * sin(centerClamped) * vNorm
        val halfWidth = width * 0.5f
        var i = 0
        for (row in 0..MESH_ROWS) {
            val v = row.toFloat() / MESH_ROWS
            val phi = centerAngle + (0.5f - v) * arc
            val screenY: Float
            val widthScale: Float
            if (phi > HALF_PI) {
                // Wrapped past the top tangent — on the back of the drum, hide far above the box.
                screenY = -OFF_BOX * height
                widthScale = 0f
            } else if (phi < -HALF_PI) {
                // Wrapped past the bottom tangent — on the back of the drum, hide far below the box.
                screenY = (1f + OFF_BOX) * height
                widthScale = 0f
            } else {
                val scale = perspScale(phi, radius, camera)
                // Curved+projected row position, blended toward the flat reference slab by cv.
                val yCyl = 0.5f - scale * radius * sin(phi) * vNorm
                val yFlat = yCenter + (v - 0.5f)
                screenY = (yFlat + (yCyl - yFlat) * cv) * height
                // Per-row horizontal scale (front rows wide, rim rows narrow), eased out with cv.
                widthScale = 1f + (scale / frontScale - 1f) * cv
            }
            val left = halfWidth - halfWidth * widthScale
            val step = (width * widthScale) / MESH_COLS
            for (col in 0..MESH_COLS) {
                verts[i++] = left + col * step
                verts[i++] = screenY
            }
        }
        paint.alpha = ALPHA_OPAQUE.toInt()
        canvas.drawBitmapMesh(bitmap, MESH_COLS, MESH_ROWS, verts, 0, null, 0, paint)
    }

    /** One-point perspective scale at drum angle [phi]: nearest (front, phi 0) projects largest. */
    private fun perspScale(phi: Float, radius: Float, camera: Float): Float =
        camera / (camera - radius * cos(phi))

    /** Cubic ease-in-out, shaping each layer's roll so it eases off the rim and settles flat. */
    private fun smoothstep(t: Float): Float = t * t * (3f - 2f * t)

    private companion object {
        // Rows carry the perspective sine curve (need to be smooth under the barrel), columns scale the
        // per-row width linearly about the centre (two is the minimum that maps the rectangle).
        const val MESH_ROWS = 32
        const val MESH_COLS = 2

        // Empty sliver between the two notches so the abutting layer edges never double-darken at the
        // seam; folded into the notch travel and eased out with the roll, so it never offsets the icon.
        const val GAP_FRACTION = 0.04f

        // Back-facing rows are pushed this many box-heights past the wrap edge — safely clipped and
        // never re-emerging, yet finite (drawBitmapMesh cannot drop vertices).
        const val OFF_BOX = 4f

        // Camera floor: distance must exceed the radius (multiple > 1) or the projection diverges.
        const val MIN_CAMERA = 1.2f

        val HALF_PI = (Math.PI / 2.0).toFloat()
    }
}
