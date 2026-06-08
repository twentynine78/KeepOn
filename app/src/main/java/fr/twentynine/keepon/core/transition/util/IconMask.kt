package fr.twentynine.keepon.core.transition.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import androidx.core.graphics.createBitmap
import java.nio.ByteBuffer

/** Full alpha (0..255) as a float, shared by the transition renderers. */
internal const val ALPHA_OPAQUE = 255f

/** Alpha above which a pixel counts as "inside the glyph" for silhouette-based transitions. */
internal const val ALPHA_THRESHOLD_FRACTION = 0.5f

/** Bitmap → ALPHA_8 mask helpers shared by the transition renderers. */
object IconMask {

    /** Coil may hand back a hardware bitmap, which cannot be drawn onto a software Canvas. */
    fun toSoftwareMask(source: Bitmap): Bitmap =
        if (source.config == Bitmap.Config.HARDWARE) {
            source.copy(Bitmap.Config.ALPHA_8, false)
        } else {
            source
        }

    /** Normalises any icon bitmap to a [width]×[height] ALPHA_8 mask (the glyph carried by alpha). */
    fun toAlphaMask(source: Bitmap, width: Int, height: Int): Bitmap {
        val mask = createBitmap(width, height, Bitmap.Config.ALPHA_8)
        val canvas = Canvas(mask)
        val software = toSoftwareMask(source)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { isFilterBitmap = true }
        if (software.width == width && software.height == height) {
            canvas.drawBitmap(software, 0f, 0f, paint)
        } else {
            val matrix = Matrix().apply {
                setScale(width.toFloat() / software.width, height.toFloat() / software.height)
            }
            canvas.drawBitmap(software, matrix, paint)
        }
        return mask
    }

    /** Reads the alpha channel of an ALPHA_8 [mask] into a width·height array of 0..1 values. */
    fun readAlpha(mask: Bitmap): FloatArray {
        val width = mask.width
        val height = mask.height
        val rowBytes = mask.rowBytes
        val buffer = ByteBuffer.allocate(rowBytes * height)
        mask.copyPixelsToBuffer(buffer)
        val raw = buffer.array()
        val alpha = FloatArray(width * height)
        for (y in 0 until height) {
            val rowBase = y * rowBytes
            val srcBase = y * width
            for (x in 0 until width) {
                alpha[srcBase + x] = (raw[rowBase + x].toInt() and 0xFF) / ALPHA_OPAQUE
            }
        }
        return alpha
    }
}
