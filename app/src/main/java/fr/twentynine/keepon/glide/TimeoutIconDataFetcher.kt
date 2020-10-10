package fr.twentynine.keepon.glide

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.Typeface
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.data.DataFetcher
import fr.twentynine.keepon.utils.KeepOnUtils
import fr.twentynine.keepon.utils.Preferences

class TimeoutIconDataFetcher(private val model: TimeoutIconData, private val context: Context) : DataFetcher<Bitmap> {
    private val Float.px: Float
        get() = (this * Resources.getSystem().displayMetrics.density)

    private val Int.px: Int
        get() = (this * Resources.getSystem().displayMetrics.density).toInt()

    override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in Bitmap>) {
        if (model.size != 3) {
            callback.onDataReady(getBitmapFromText(model.timeout, context, model.size == 1))
        } else {
            callback.onDataReady(getShortcutBitmapFromText(model.timeout, context))
        }
    }

    override fun getDataClass(): Class<Bitmap> {
        return Bitmap::class.java
    }

    override fun getDataSource(): DataSource {
        return DataSource.LOCAL
    }

    override fun cleanup() {}
    override fun cancel() {}

    private fun getBitmapFromText(timeout: Int, context: Context, bigSize: Boolean = false): Bitmap {
        // Set scale ratio to 3 for small size
        val scaleRatio = if (bigSize) 1 else 3

        val imageWidth = 150.px / scaleRatio
        val imageHeight = 150.px / scaleRatio

        val displayTimeout = KeepOnUtils.getDisplayTimeout(timeout, context)
        val bitmap = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint()

        canvas.drawColor(Color.TRANSPARENT)

        var textSize = when (displayTimeout.length) {
            1 -> 115f.px / scaleRatio
            2 -> 86f.px / scaleRatio
            3 -> 70f.px / scaleRatio
            else -> 60f.px / scaleRatio
        }
        // Set text size from saved preference
        textSize += (Preferences.getQSStyleFontSize(context) * 2).px / scaleRatio
        paint.textSize = textSize

        // Set typeface from saved preference
        val bold = Preferences.getQSStyleFontBold(context)
        val typeface: Typeface = when {
            Preferences.getQSStyleTypefaceSansSerif(context) -> {
                if (bold) {
                    Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                } else {
                    Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                }
            }
            Preferences.getQSStyleTypefaceSerif(context) -> {
                if (bold) {
                    Typeface.create(Typeface.SERIF, Typeface.BOLD)
                } else {
                    Typeface.create(Typeface.SERIF, Typeface.NORMAL)
                }
            }
            Preferences.getQSStyleTypefaceMonospace(context) -> {
                if (bold) {
                    Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                } else {
                    Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
                }
            }
            else -> Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        paint.typeface = typeface

        // Set text style from saved preference
        val paintStyle: Paint.Style = when {
            Preferences.getQSStyleTextFill(context) -> {
                Paint.Style.FILL
            }
            Preferences.getQSStyleTextFillStroke(context) -> {
                paint.strokeWidth = 3f.px / scaleRatio
                Paint.Style.FILL_AND_STROKE
            }
            Preferences.getQSStyleTextStroke(context) -> {
                paint.strokeWidth = 3f.px / scaleRatio
                Paint.Style.STROKE
            }
            else -> Paint.Style.FILL
        }
        paint.style = paintStyle

        // Set text skew from preference
        paint.textSkewX = (-(Preferences.getQSStyleFontSkew(context) / 1.7).toFloat())

        // Set font SMCP from preference
        if (Preferences.getQSStyleFontSMCP(context)) {
            paint.fontFeatureSettings = "smcp"
        }

        // Set font underline from preference
        paint.isUnderlineText = Preferences.getQSStyleFontUnderline(context)

        paint.textAlign = Paint.Align.LEFT
        paint.isAntiAlias = true
        paint.color = Color.WHITE

        // Calculate coordinates
        val rect = Rect()
        canvas.getClipBounds(rect)
        val canvasHeight = rect.height()
        val canvasWidth = rect.width()
        paint.getTextBounds(displayTimeout, 0, displayTimeout.length, rect)
        val x = canvasWidth / 2f - rect.width() / 2f - rect.left
        val y = canvasHeight / 2f + rect.height() / 2f - rect.bottom

        // Draw text
        canvas.drawText(
            displayTimeout,
            x + ((Preferences.getQSStyleFontSpacing(context) * 7).px / scaleRatio),
            y,
            paint
        )
        canvas.save()
        canvas.restore()

        return bitmap
    }

    private fun getShortcutBitmapFromText(timeout: Int, context: Context): Bitmap {
        val imageWidth = 25.px
        val imageHeight = 25.px

        val textColor = Color.parseColor("#FF3B3B3B")
        val shadowColor = Color.parseColor("#82222222")

        val displayTimeout = KeepOnUtils.getDisplayTimeout(timeout, context)

        val bitmap = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint()
        paint.isAntiAlias = true
        paint.style = Paint.Style.FILL
        paint.color = Color.WHITE
        paint.setShadowLayer(2f, 1f, 1f, shadowColor)

        canvas.drawCircle(
            (imageWidth / 2).toFloat(),
            (imageHeight / 2).toFloat(),
            11.px.toFloat(),
            paint
        )

        paint.style = Paint.Style.STROKE
        paint.color = textColor
        paint.strokeWidth = 2f
        paint.strokeCap = Paint.Cap.ROUND

        canvas.drawCircle(
            (imageWidth / 2).toFloat(),
            (imageHeight / 2).toFloat(),
            11.px.toFloat(),
            paint
        )

        var textSize = when (displayTimeout.length) {
            1 -> 13f.px
            2 -> 9f.px
            3 -> 7f.px
            else -> 5f.px
        }
        // Set text size from saved preference
        textSize += (Preferences.getQSStyleFontSize(context) / 2).px
        paint.textSize = textSize

        // Set typeface from saved preference
        val bold = Preferences.getQSStyleFontBold(context)
        val typeface: Typeface = when {
            Preferences.getQSStyleTypefaceSansSerif(context) -> {
                if (bold) {
                    Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                } else {
                    Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                }
            }
            Preferences.getQSStyleTypefaceSerif(context) -> {
                if (bold) {
                    Typeface.create(Typeface.SERIF, Typeface.BOLD)
                } else {
                    Typeface.create(Typeface.SERIF, Typeface.NORMAL)
                }
            }
            Preferences.getQSStyleTypefaceMonospace(context) -> {
                if (bold) {
                    Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                } else {
                    Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
                }
            }
            else -> Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        paint.typeface = typeface

        // Set text style from saved preference
        val paintStyle: Paint.Style = when {
            Preferences.getQSStyleTextFill(context) -> {
                Paint.Style.FILL
            }
            Preferences.getQSStyleTextFillStroke(context) -> {
                paint.strokeWidth = 1f.px
                Paint.Style.FILL_AND_STROKE
            }
            Preferences.getQSStyleTextStroke(context) -> {
                paint.strokeWidth = 1f.px
                Paint.Style.STROKE
            }
            else -> Paint.Style.FILL
        }
        paint.style = paintStyle
        paint.strokeCap = Paint.Cap.ROUND

        // Set text skew from preference
        paint.textSkewX = (-(Preferences.getQSStyleFontSkew(context) / 1.7).toFloat())

        // Set font SMCP from preference
        if (Preferences.getQSStyleFontSMCP(context)) {
            paint.fontFeatureSettings = "smcp"
        }

        // Set font underline from preference
        paint.isUnderlineText = Preferences.getQSStyleFontUnderline(context)

        paint.textAlign = Paint.Align.LEFT
        paint.isAntiAlias = true
        paint.color = textColor
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)

        // Calculate coordinates
        val rect = Rect(0, 0, imageWidth, imageHeight)
        val canvasHeight = rect.height()
        val canvasWidth = rect.width()

        paint.setShadowLayer(1f, 1f, 1f, shadowColor)

        canvas.save()

        // If it is not restore timeout, draw text
        paint.getTextBounds(displayTimeout, 0, displayTimeout.length, rect)
        val x = canvasWidth / 2f - rect.width() / 2f - rect.left
        val y = canvasHeight / 2f + rect.height() / 2f - rect.bottom

        // Draw text
        canvas.drawText(
            displayTimeout,
            x + (Preferences.getQSStyleFontSpacing(context) / 2).px,
            y,
            paint
        )
        canvas.save()
        canvas.restore()

        return bitmap
    }
}
