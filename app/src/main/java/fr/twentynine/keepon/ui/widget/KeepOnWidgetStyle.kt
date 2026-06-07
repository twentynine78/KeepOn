package fr.twentynine.keepon.ui.widget

import android.graphics.Bitmap
import android.graphics.Paint
import android.os.Build
import androidx.annotation.ColorInt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.applyCanvas
import androidx.core.graphics.createBitmap
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.background
import androidx.glance.unit.ColorProvider
import fr.twentynine.keepon.ui.widget.KeepOnWidget.Companion.SMALL_SQUARE

/**
 * Resolved colors for the widget, derived from the active/inactive state. Bundled so the live
 * content ([KeepOnWidgetView]) and the preview ([KeepOnWidgetPreview]) build them the same way —
 * the preview is simply the inactive palette.
 */
class WidgetColors(
    val borderColor: Color,
    val backgroundColor: ColorProvider,
    val widgetBackgroundColor: Color,
    val imageColorFilter: ColorFilter,
    val contentColor: ColorProvider,
)

@Composable
fun rememberWidgetColors(keepOnIsActive: Boolean): WidgetColors {
    val context = LocalContext.current
    return remember(keepOnIsActive) {
        val scheme = KeepOnWidgetColorScheme.colors
        val border = if (keepOnIsActive) scheme.background else scheme.primaryContainer
        val background = if (keepOnIsActive) scheme.primaryContainer else scheme.background
        val content = if (keepOnIsActive) scheme.onPrimaryContainer else scheme.onBackground
        WidgetColors(
            borderColor = border.getColor(context),
            backgroundColor = background,
            widgetBackgroundColor = scheme.widgetBackground.getColor(context).copy(alpha = WIDGET_BACKGROUND_COLOR_ALPHA),
            imageColorFilter = ColorFilter.tint(content),
            contentColor = content,
        )
    }
}

/**
 * Widget dimensions derived from the smallest cell size, shared by the content and loading
 * layouts so they stay visually consistent across the responsive sizes.
 */
class WidgetDimens(
    val widgetMinSize: Dp,
    val cornerRadius: Dp,
    val outerBoxPadding: Dp,
    val borderSize: Dp,
    val imagePadding: Dp,
)

@Composable
fun rememberWidgetDimens(): WidgetDimens {
    val currentWidth = LocalSize.current.width
    val widgetMinSize = if (currentWidth != 0.dp) currentWidth else SMALL_SQUARE.width
    return remember(widgetMinSize) {
        val outerBoxPadding = widgetMinSize / OUTER_BOX_PADDING_RATIO
        WidgetDimens(
            widgetMinSize = widgetMinSize,
            cornerRadius = widgetMinSize / CORNER_RADIUS_RATIO,
            outerBoxPadding = outerBoxPadding,
            borderSize = outerBoxPadding / BORDER_SIZE_RATIO,
            imagePadding = widgetMinSize / IMAGE_PADDING_RATIO,
        )
    }
}

/*
 * Rounded-corner fallback for Android 9/10/11 (API < 31), where GlanceModifier.cornerRadius() is a
 * no-op. The rounded look is provided by a pre-rendered bitmap used as a background ImageProvider,
 * which is honoured on every API. The widget shape is a circle (radius = size / 2), so an
 * anti-aliased filled circle reproduces it exactly and is size-independent.
 */

/** True on Android versions where cornerRadius() does not round (needs the bitmap fallback). */
private val needsLegacyRoundedBackground: Boolean
    get() = Build.VERSION.SDK_INT < Build.VERSION_CODES.S

private fun circleBitmap(sizePx: Int, @ColorInt color: Int): Bitmap {
    val radius = sizePx / 2f
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color }
    return createBitmap(sizePx, sizePx).applyCanvas {
        drawCircle(radius, radius, radius, paint)
    }
}

private fun concentricCirclesBitmap(
    sizePx: Int,
    outerPaddingPx: Float,
    borderPx: Float,
    @ColorInt outerColor: Int,
    @ColorInt borderColor: Int,
    @ColorInt innerColor: Int,
): Bitmap {
    val center = sizePx / 2f
    val borderRadius = (center - outerPaddingPx).coerceAtLeast(0f)
    val innerRadius = (borderRadius - borderPx).coerceAtLeast(0f)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    return createBitmap(sizePx, sizePx).applyCanvas {
        paint.color = outerColor
        drawCircle(center, center, center, paint)
        paint.color = borderColor
        drawCircle(center, center, borderRadius, paint)
        paint.color = innerColor
        drawCircle(center, center, innerRadius, paint)
    }
}

/**
 * The single composite background for [KeepOnWidgetContent] on API < 31: the three concentric
 * circles (translucent margin, border, inner fill) drawn into one bitmap. Returns null on API 31+,
 * where the per-box cornerRadius() + color path is used instead.
 */
@Composable
fun rememberLegacyWidgetBackground(
    borderColor: Color,
    backgroundColor: ColorProvider,
    widgetBackgroundColor: Color,
): ImageProvider? {
    if (!needsLegacyRoundedBackground) return null
    val context = LocalContext.current
    val dimens = rememberWidgetDimens()
    val density = context.resources.displayMetrics.density
    val borderArgb = borderColor.toArgb()
    val backgroundArgb = backgroundColor.getColor(context).toArgb()
    val widgetBackgroundArgb = widgetBackgroundColor.toArgb()
    val outerSizeDp = (dimens.widgetMinSize + dimens.outerBoxPadding * 2).value
    val outerPaddingDp = dimens.outerBoxPadding.value
    val borderDp = dimens.borderSize.value
    return remember(borderArgb, backgroundArgb, widgetBackgroundArgb, outerSizeDp, density) {
        val sizePx = (outerSizeDp * density).toInt().coerceIn(1, LEGACY_BACKGROUND_MAX_PX)
        val scale = sizePx / outerSizeDp
        ImageProvider(
            concentricCirclesBitmap(
                sizePx = sizePx,
                outerPaddingPx = outerPaddingDp * scale,
                borderPx = borderDp * scale,
                outerColor = widgetBackgroundArgb,
                borderColor = borderArgb,
                innerColor = backgroundArgb,
            )
        )
    }
}

/** Single-circle background for the loading layout on API < 31 (null on API 31+). */
@Composable
fun rememberLegacyCircleBackground(color: ColorProvider): ImageProvider? {
    if (!needsLegacyRoundedBackground) return null
    val context = LocalContext.current
    val dimens = rememberWidgetDimens()
    val density = context.resources.displayMetrics.density
    val argb = color.getColor(context).toArgb()
    val sizeDp = dimens.widgetMinSize.value
    return remember(argb, sizeDp, density) {
        val sizePx = (sizeDp * density).toInt().coerceIn(1, LEGACY_BACKGROUND_MAX_PX)
        ImageProvider(circleBitmap(sizePx, argb))
    }
}

/** Root background: the legacy bitmap when present (API < 31), otherwise the solid color. */
fun GlanceModifier.applyRootBackground(legacy: ImageProvider?, color: Color): GlanceModifier =
    if (legacy != null) background(legacy) else background(color)

fun GlanceModifier.applyRootBackground(legacy: ImageProvider?, color: ColorProvider): GlanceModifier =
    if (legacy != null) background(legacy) else background(color)

/**
 * Inner-layer background: skipped when the legacy composite is in use (the layer is already painted
 * inside the root bitmap), otherwise the solid color (API 31+).
 */
fun GlanceModifier.applyLayerBackground(legacy: ImageProvider?, color: Color): GlanceModifier =
    if (legacy != null) this else background(color)

fun GlanceModifier.applyLayerBackground(legacy: ImageProvider?, color: ColorProvider): GlanceModifier =
    if (legacy != null) this else background(color)
