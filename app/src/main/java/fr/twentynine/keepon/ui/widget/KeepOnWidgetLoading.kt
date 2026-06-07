package fr.twentynine.keepon.ui.widget

import androidx.compose.runtime.Composable
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.CircularProgressIndicator
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.padding
import androidx.glance.layout.size

@Composable
fun KeepOnWidgetLoading(
    modifier: GlanceModifier = GlanceModifier,
) {
    val dimens = rememberWidgetDimens()
    val widgetMinSize = dimens.widgetMinSize
    val cornerRadius = dimens.cornerRadius
    val imagePadding = dimens.imagePadding

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
