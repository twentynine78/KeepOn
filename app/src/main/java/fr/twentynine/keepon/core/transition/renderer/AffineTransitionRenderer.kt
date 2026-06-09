package fr.twentynine.keepon.core.transition.renderer

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import androidx.core.graphics.createBitmap
import fr.twentynine.keepon.core.transition.IconTransitionRenderer
import fr.twentynine.keepon.core.transition.TransitionFrames
import fr.twentynine.keepon.core.transition.util.ALPHA_OPAQUE
import fr.twentynine.keepon.core.transition.util.IconMask
import fr.twentynine.keepon.domain.model.AffineTransition
import fr.twentynine.keepon.domain.model.LayerTransform
import kotlin.math.cos

private const val DEGREES_TO_RADIANS = (Math.PI / 180.0).toFloat()

/**
 * Composites the outgoing and incoming layers with a translate/scale/alpha transform (plus a
 * vertical-squash approximation of the 3D `rotationX`).
 */
class AffineTransitionRenderer(private val transition: AffineTransition) : IconTransitionRenderer {

    override fun prepare(from: Bitmap, to: Bitmap): TransitionFrames {
        val softFrom = IconMask.toSoftwareMask(from)
        val softTo = IconMask.toSoftwareMask(to)
        return TransitionFrames { progress ->
            val (oldTransform, newTransform) = transition.transformsAt(progress)
            buildFrame(softFrom, softTo, oldTransform, newTransform)
        }
    }

    private fun buildFrame(
        from: Bitmap,
        to: Bitmap,
        oldTransform: LayerTransform,
        newTransform: LayerTransform,
    ): Bitmap {
        val width = to.width
        val height = to.height
        val output = createBitmap(width, height, Bitmap.Config.ALPHA_8)
        val canvas = Canvas(output)
        drawLayer(canvas, from, oldTransform)
        drawLayer(canvas, to, newTransform)
        return output
    }

    private fun drawLayer(canvas: Canvas, bitmap: Bitmap, transform: LayerTransform) {
        // rotationX is faked on the 2D canvas by squashing vertically (cos of the angle) about the
        // centre; past 90° the layer faces away and is skipped.
        val cosAngle = cos(transform.rotationX * DEGREES_TO_RADIANS)
        if (cosAngle <= 0f || transform.alpha <= 0f) return

        val width = bitmap.width.toFloat()
        val height = bitmap.height.toFloat()
        val matrix = Matrix().apply {
            postScale(transform.scaleX, transform.scaleY * cosAngle, width / 2f, height / 2f)
            postTranslate(transform.translationXFraction * width, transform.translationYFraction * height)
        }
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            isFilterBitmap = true
            alpha = (transform.alpha.coerceIn(0f, 1f) * ALPHA_OPAQUE).toInt()
        }
        canvas.drawBitmap(bitmap, matrix, paint)
    }
}
