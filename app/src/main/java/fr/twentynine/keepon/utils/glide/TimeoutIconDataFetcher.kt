package fr.twentynine.keepon.utils.glide

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
import fr.twentynine.keepon.di.ToothpickHelper
import fr.twentynine.keepon.utils.CommonUtils
import fr.twentynine.keepon.utils.preferences.Preferences
import fr.twentynine.keepon.utils.px
import toothpick.ktp.delegate.lazy

class TimeoutIconDataFetcher(private val model: TimeoutIconData) : DataFetcher<Bitmap> {

    private val commonUtils: CommonUtils by lazy()
    private val preferences: Preferences by lazy()

    init {
        // Inject dependencies with Toothpick
        ToothpickHelper.scopedInjection(this)
    }

    override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in Bitmap>) {
        if (model.size != 3) {
            callback.onDataReady(getBitmapFromText(model.timeout, model.size == 1))
        } else {
            callback.onDataReady(getShortcutBitmapFromText(model.timeout))
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

    private fun getBitmapFromText(timeout: Int, bigSize: Boolean = false): Bitmap {
        // Set scale ratio to 3 for small size
        val scaleRatio = if (bigSize) 1 else 3

        val imageWidth = 150.px / scaleRatio
        val imageHeight = 150.px / scaleRatio

        val displayTimeout = commonUtils.getDisplayTimeout(timeout)
        val bitmap = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint()

        canvas.drawColor(colorTransparent)

        var textSize = when (displayTimeout.length) {
            1 -> 115f.px / scaleRatio
            2 -> 86f.px / scaleRatio
            3 -> 70f.px / scaleRatio
            else -> 60f.px / scaleRatio
        }
        // Set text size from saved preference
        textSize += (preferences.getQSStyleFontSize() * 2).px / scaleRatio
        paint.textSize = textSize

        // Set typeface from saved preference
        val bold = preferences.getQSStyleFontBold()
        paint.typeface = when {
            preferences.getQSStyleTypefaceSansSerif() -> {
                if (bold) {
                    sansSerifBold
                } else {
                    sansSerifNormal
                }
            }
            preferences.getQSStyleTypefaceSerif() -> {
                if (bold) {
                    serifBold
                } else {
                    serifNormal
                }
            }
            preferences.getQSStyleTypefaceMonospace() -> {
                if (bold) {
                    monospaceBold
                } else {
                    monospaceNormal
                }
            }
            else -> sansSerifBold
        }

        // Set text style from saved preference
        val paintStyle = when {
            preferences.getQSStyleTextFill() -> {
                paintStyleFill
            }
            preferences.getQSStyleTextFillStroke() -> {
                paint.strokeWidth = 3f.px / scaleRatio
                paintStyleFillAndStroke
            }
            preferences.getQSStyleTextStroke() -> {
                paint.strokeWidth = 3f.px / scaleRatio
                paintStyleStroke
            }
            else -> paintStyleFill
        }
        paint.style = paintStyle

        // Set text skew from preference
        paint.textSkewX = (-(preferences.getQSStyleFontSkew() / 1.7).toFloat())

        // Set font SMCP from preference
        if (preferences.getQSStyleFontSMCP()) {
            paint.fontFeatureSettings = "smcp"
        }

        // Set font underline from preference
        paint.isUnderlineText = preferences.getQSStyleFontUnderline()

        paint.textAlign = paintAlignLeft
        paint.isAntiAlias = true
        paint.color = colorWhite

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
            x + ((preferences.getQSStyleFontSpacing() * 7).px / scaleRatio),
            y,
            paint
        )
        canvas.save()
        canvas.restore()

        return bitmap
    }

    private fun getShortcutBitmapFromText(timeout: Int): Bitmap {
        val imageWidth = 25.px
        val imageHeight = 25.px

        val displayTimeout = commonUtils.getDisplayTimeout(timeout)

        val bitmap = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint()
        paint.isAntiAlias = true
        paint.style = paintStyleFill
        paint.color = colorWhite
        paint.setShadowLayer(2f, 1f, 1f, shadowColor)

        canvas.drawCircle(
            (imageWidth / 2).toFloat(),
            (imageHeight / 2).toFloat(),
            11.px.toFloat(),
            paint
        )

        paint.style = paintStyleStroke
        paint.color = textColor
        paint.strokeWidth = 2f
        paint.strokeCap = paintCapRound

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
        textSize += (preferences.getQSStyleFontSize() / 2).px
        paint.textSize = textSize

        // Set typeface from saved preference
        val bold = preferences.getQSStyleFontBold()
        paint.typeface = when {
            preferences.getQSStyleTypefaceSansSerif() -> {
                if (bold) {
                    sansSerifBold
                } else {
                    sansSerifNormal
                }
            }
            preferences.getQSStyleTypefaceSerif() -> {
                if (bold) {
                    serifBold
                } else {
                    serifNormal
                }
            }
            preferences.getQSStyleTypefaceMonospace() -> {
                if (bold) {
                    monospaceBold
                } else {
                    monospaceNormal
                }
            }
            else -> sansSerifBold
        }

        // Set text style from saved preference
        val paintStyle = when {
            preferences.getQSStyleTextFill() -> {
                paintStyleFill
            }
            preferences.getQSStyleTextFillStroke() -> {
                paint.strokeWidth = 1f.px
                paintStyleFillAndStroke
            }
            preferences.getQSStyleTextStroke() -> {
                paint.strokeWidth = 1f.px
                paintStyleStroke
            }
            else -> paintStyleFill
        }
        paint.style = paintStyle
        paint.strokeCap = paintCapRound

        // Set text skew from preference
        paint.textSkewX = (-(preferences.getQSStyleFontSkew() / 1.7).toFloat())

        // Set font SMCP from preference
        if (preferences.getQSStyleFontSMCP()) {
            paint.fontFeatureSettings = "smcp"
        }

        // Set font underline from preference
        paint.isUnderlineText = preferences.getQSStyleFontUnderline()

        paint.textAlign = paintAlignLeft
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
            x + (preferences.getQSStyleFontSpacing() / 2).px,
            y,
            paint
        )
        canvas.save()
        canvas.restore()

        return bitmap
    }

    companion object {
        private val sansSerifBold by lazy { Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD) }
        private val sansSerifNormal by lazy { Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL) }
        private val serifBold by lazy { Typeface.create(Typeface.SERIF, Typeface.BOLD) }
        private val serifNormal by lazy { Typeface.create(Typeface.SERIF, Typeface.NORMAL) }
        private val monospaceBold by lazy { Typeface.create(Typeface.MONOSPACE, Typeface.BOLD) }
        private val monospaceNormal by lazy { Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL) }

        private val paintStyleFill by lazy { Paint.Style.FILL }
        private val paintStyleFillAndStroke by lazy { Paint.Style.FILL_AND_STROKE }
        private val paintStyleStroke by lazy { Paint.Style.STROKE }
        private val paintCapRound by lazy { Paint.Cap.ROUND }
        private val paintAlignLeft by lazy { Paint.Align.LEFT }

        private val colorTransparent by lazy { Color.TRANSPARENT }
        private val colorWhite by lazy { Color.WHITE }
        private val textColor by lazy { Color.parseColor("#FF3B3B3B") }
        private val shadowColor by lazy { Color.parseColor("#82222222") }
    }
}
