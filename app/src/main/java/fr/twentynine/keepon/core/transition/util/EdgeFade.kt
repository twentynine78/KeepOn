package fr.twentynine.keepon.core.transition.util

import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Shader
import fr.twentynine.keepon.domain.model.FadingEdge

/**
 * Soft-fades the requested edges of a frame (DST_IN gradient) so sliding content appears and
 * disappears gently at the boundary. The opaque band in the middle stays wide enough not to touch a
 * centred glyph, and the renderers only apply this to the moving frames (not the settled icon).
 */
object EdgeFade {

    private const val TRANSPARENT = 0x00000000
    private const val OPAQUE = 0xFF000000.toInt()

    fun apply(canvas: Canvas, width: Float, height: Float, fadingEdge: FadingEdge) {
        if (fadingEdge.fadesVertical) applyAxis(canvas, width, height, vertical = true)
        if (fadingEdge.fadesHorizontal) applyAxis(canvas, width, height, vertical = false)
    }

    private fun applyAxis(canvas: Canvas, width: Float, height: Float, vertical: Boolean) {
        val gradient = LinearGradient(
            0f, 0f, if (vertical) 0f else width, if (vertical) height else 0f,
            intArrayOf(TRANSPARENT, OPAQUE, OPAQUE, TRANSPARENT),
            floatArrayOf(0f, FadingEdge.FADE_FRACTION, 1f - FadingEdge.FADE_FRACTION, 1f),
            Shader.TileMode.CLAMP,
        )
        val paint = Paint().apply {
            shader = gradient
            xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
        }
        canvas.drawRect(0f, 0f, width, height, paint)
    }
}
