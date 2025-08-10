package fr.twentynine.keepon.util.coil

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.text.TextPaint
import androidx.core.graphics.createBitmap
import fr.twentynine.keepon.data.enums.TimeoutIconSize
import fr.twentynine.keepon.data.local.IconFontFamily
import fr.twentynine.keepon.data.model.TimeoutIconData
import fr.twentynine.keepon.data.repo.IconFontFamilyRepository
import fr.twentynine.keepon.util.StringResourceProvider
import fr.twentynine.keepon.util.extensions.px

class TimeoutIconGenerator {

    fun getBitmapFromText(
        context: Context,
        model: TimeoutIconData,
        stringResourceProvider: StringResourceProvider,
    ): Bitmap {
        val timeout = model.iconTimeout
        val iconSize = model.iconSize
        val iconStyle = model.iconStyle
        val iconFontFamily = IconFontFamilyRepository.iconFontFamilies.getValue(iconStyle.iconFontFamilyName)
        val boldFont = iconStyle.iconStyleFontBold
        val italicFont = iconStyle.iconStyleFontItalic
        val iconTypeface = getIconTypeface(iconFontFamily, boldFont, italicFont)
        val underlineFont = iconStyle.iconStyleFontUnderline
        val outlinedFont = iconStyle.iconStyleTextOutlined
        val fontSize = iconStyle.iconStyleFontSize
        val horizontalPadding = iconStyle.iconStyleFontHorizontalSpacing
        val verticalPadding = iconStyle.iconStyleFontVerticalSpacing

        // Set scale ratio to manage large and medium sizes
        val scaleRatio = TimeoutIconSize.LARGE.size / iconSize.size

        val imageWidth = iconSize.size.px
        val imageHeight = iconSize.size.px

        val displayTimeout = timeout.getShortDisplayTimeout(stringResourceProvider)
        val bitmap = createBitmap(imageWidth, imageHeight, Bitmap.Config.ALPHA_8)
        val canvas = Canvas(bitmap)

        val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)

        // Set background
        val backgroundRect = Rect()
        val backgroundPaint = Paint()

        backgroundRect.set(0, 0, imageWidth, imageHeight)

        backgroundPaint.style = Paint.Style.FILL_AND_STROKE
        backgroundPaint.color = Color.TRANSPARENT

        canvas.drawRect(backgroundRect, backgroundPaint)

        // Set typeface from user preference
        textPaint.typeface = context.resources.getFont(iconTypeface)

        // Set text style from user preference
        if (outlinedFont) {
            textPaint.strokeWidth = STROKE_WIDTH.px / scaleRatio
            textPaint.style = Paint.Style.STROKE
        }

        // Set font underline from user preference
        textPaint.isUnderlineText = underlineFont

        // Set TextPaint properties
        textPaint.isAntiAlias = true

        val textBounds = setTextSizeAndGetBounds(
            textPaint,
            displayTimeout,
            imageWidth,
            imageHeight,
            fontSize,
            scaleRatio,
        )

        // Calculate Text Position
        val horizontalPaddingPx = (horizontalPadding * HORIZONTAL_PADDING_STEP_COEF).px / scaleRatio
        val verticalPaddingPx = (verticalPadding * VERTICAL_PADDING_STEP_COEF).px / scaleRatio
        val textX = backgroundRect.centerX().toFloat() - textBounds.centerX() + horizontalPaddingPx
        val textY = backgroundRect.centerY().toFloat() - textBounds.centerY() - verticalPaddingPx

        // Draw text
        canvas.drawText(
            displayTimeout,
            textX,
            textY,
            textPaint
        )

        return bitmap
    }

    private fun getIconTypeface(iconFontFamily: IconFontFamily, boldFont: Boolean, italicFont: Boolean): Int {
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
        scaleRatio: Int
    ): Rect {
        val bounds = Rect()

        // Calculate text size with a default text size
        val textSize = DEFAULT_TEXT_SIZE.px
        textPaint.textSize = textSize

        textPaint.getTextBounds(text, 0, text.length, bounds)

        var desiredTextSize = textSize * imageWidth / bounds.width().toFloat()

        // Add font size value from user preference
        desiredTextSize += (fontSize * FONT_SIZE_STEP_COEF) / scaleRatio
        textPaint.textSize = desiredTextSize - textPaint.strokeWidth

        // Get text bounds
        textPaint.getTextBounds(text, 0, text.length, bounds)

        // If the text is too height, reduce text size
        if (bounds.height().toFloat() / imageHeight > MAX_IMAGE_HEIGHT_PERCENT) {
            val maxImageHeight = imageHeight * MAX_IMAGE_HEIGHT_PERCENT
            val newTextSize = desiredTextSize * maxImageHeight / bounds.height().toFloat()

            textPaint.textSize = newTextSize
            textPaint.getTextBounds(text, 0, text.length, bounds)
        }

        return bounds
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
