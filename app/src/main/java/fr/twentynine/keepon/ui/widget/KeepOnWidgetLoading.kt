package fr.twentynine.keepon.ui.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.LocalSize
import androidx.glance.appwidget.CircularProgressIndicator
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.padding
import androidx.glance.layout.size
import fr.twentynine.keepon.widget.KeepOnWidget.Companion.SMALL_SQUARE

@Composable
fun KeepOnWidgetLoading(
    modifier: GlanceModifier = GlanceModifier,
) {
    val currentWidth = LocalSize.current.width
    val widgetMinSize = if (currentWidth != 0.dp) { currentWidth } else { SMALL_SQUARE.width }
    val cornerRadius = widgetMinSize / CORNER_RADIUS_RATIO
    val imagePadding = widgetMinSize / IMAGE_PADDING_RATIO

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .appWidgetBackground()
            .cornerRadius(cornerRadius)
            .size(widgetMinSize)
            .background(KeepOnWidgetColorScheme.colors.background)
            .padding(imagePadding),
    ) {
        CircularProgressIndicator(
            color = KeepOnWidgetColorScheme.colors.onBackground
        )
    }
}
