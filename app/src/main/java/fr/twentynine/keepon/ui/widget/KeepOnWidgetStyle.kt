package fr.twentynine.keepon.ui.widget

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.glance.ColorFilter
import androidx.glance.LocalContext
import androidx.glance.LocalSize
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
