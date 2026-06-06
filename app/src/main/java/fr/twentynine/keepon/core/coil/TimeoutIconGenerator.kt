package fr.twentynine.keepon.core.coil

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.text.TextPaint
import androidx.core.graphics.createBitmap
import fr.twentynine.keepon.domain.model.TimeoutIconSize
import fr.twentynine.keepon.domain.catalog.IconFontFamily
import fr.twentynine.keepon.domain.model.TimeoutIconData
import fr.twentynine.keepon.domain.catalog.IconFontFamilyCatalog
import fr.twentynine.keepon.domain.gateway.StringResourceProvider
import fr.twentynine.keepon.core.util.px

class TimeoutIconGenerator {

    fun getBitmapFromText(
        context: Context,
        model: TimeoutIconData,
        stringResourceProvider: StringResourceProvider,
    ): Bitmap {
        val timeout = model.iconTimeout
        val iconSize = model.iconSize
        val iconStyle = model.iconStyle
        val iconFontFamily = IconFontFamilyCatalog.iconFontFamilies.getValue(iconStyle.iconFontFamilyName)
        val boldFont = iconStyle.iconStyleFontBold
        val italicFont = iconStyle.iconStyleFontItalic
        val iconTypeface = getIconTypefaceId(iconFontFamily, boldFont, italicFont)
        val underlineFont = iconStyle.iconStyleFontUnderline
        val outlinedFont = iconStyle.iconStyleTextOutlined
        val fontSize = iconStyle.iconStyleFontSize
        val horizontalPadding = iconStyle.iconStyleFontHorizontalSpacing
        val verticalPadding = iconStyle.iconStyleFontVerticalSpacing

        // Scale ratio relative to the largest icon size, so MEDIUM icons get
        // proportionally smaller stroke/padding/font offsets than LARGE.
        val scaleRatio = TimeoutIconSize.LARGE.size.toFloat() / iconSize.size

        val imageWidth = iconSize.size.px
        val imageHeight = iconSize.size.px

        val displayTimeout = timeout.getShortDisplayTimeout(stringResourceProvider)

        val bitmap = createBitmap(imageWidth, imageHeight, Bitmap.Config.ALPHA_8)
        val canvas = Canvas(bitmap)

        // Apply typeface and text style from user preference
        val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            isAntiAlias = true
            typeface = context.resources.getFont(iconTypeface)
            style = if (outlinedFont) Paint.Style.STROKE else Paint.Style.FILL_AND_STROKE
            strokeWidth = if (outlinedFont) (STROKE_WIDTH.px / scaleRatio) else 0f
            isUnderlineText = underlineFont
        }

        val textBounds = Rect()
        setTextSizeAndGetBounds(
            textPaint,
            displayTimeout,
            imageWidth,
            imageHeight,
            fontSize,
            scaleRatio,
            textBounds,
        )

        // Calculate Text Position
        val horizontalPaddingPx = (horizontalPadding * HORIZONTAL_PADDING_STEP_COEF).px / scaleRatio
        val verticalPaddingPx = (verticalPadding * VERTICAL_PADDING_STEP_COEF).px / scaleRatio
        val centerX = (imageWidth / 2).toFloat()
        val centerY = (imageHeight / 2).toFloat()
        val textX = centerX - textBounds.centerX() + horizontalPaddingPx
        val textY = centerY - textBounds.centerY() - verticalPaddingPx

        // Draw text
        canvas.drawText(
            displayTimeout,
            textX,
            textY,
            textPaint
        )

        return bitmap
    }

    private fun getIconTypefaceId(iconFontFamily: IconFontFamily, boldFont: Boolean, italicFont: Boolean): Int {
        return when {
            boldFont && italicFont -> iconFontFamily.boldItalicTypefaceId
            boldFont -> iconFontFamily.boldTypefaceId
            italicFont -> iconFontFamily.italicTypefaceId
            else -> iconFontFamily.regularTypefaceId
        }
    }

    private fun setTextSizeAndGetBounds(
        textPaint: TextPaint,
        text: String,
        imageWidth: Int,
        imageHeight: Int,
        fontSize: Int,
        scaleRatio: Float,
        outBounds: Rect
    ) {
        // Calculate text size with a default text size
        val textSize = DEFAULT_TEXT_SIZE.px
        textPaint.textSize = textSize

        textPaint.getTextBounds(text, 0, text.length, outBounds)

        var desiredTextSize = textSize * imageWidth / outBounds.width().toFloat()

        // Add font size value from user preference
        desiredTextSize += (fontSize * FONT_SIZE_STEP_COEF) / scaleRatio
        textPaint.textSize = desiredTextSize - textPaint.strokeWidth

        // Get text bounds
        textPaint.getTextBounds(text, 0, text.length, outBounds)

        // If the text is too height, reduce text size
        if (outBounds.height().toFloat() / imageHeight > MAX_IMAGE_HEIGHT_PERCENT) {
            val maxImageHeight = imageHeight * MAX_IMAGE_HEIGHT_PERCENT
            val newTextSize = desiredTextSize * maxImageHeight / outBounds.height().toFloat()

            textPaint.textSize = newTextSize
            textPaint.getTextBounds(text, 0, text.length, outBounds)
        }
    }

    companion object {
        const val STROKE_WIDTH = 1f
        const val DEFAULT_TEXT_SIZE = 20f

        const val FONT_SIZE_STEP_COEF = 4
        const val HORIZONTAL_PADDING_STEP_COEF = 2
        const val VERTICAL_PADDING_STEP_COEF = 2
        const val MAX_IMAGE_HEIGHT_PERCENT = 0.6f
    }
}
