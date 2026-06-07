package fr.twentynine.keepon.ui.widget

import androidx.compose.runtime.Composable
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.CircularProgressIndicator
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
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

    // On API < 31 cornerRadius() is a no-op; round the loading circle with a bitmap fallback.
    val legacyBackground = rememberLegacyCircleBackground(KeepOnWidgetColorScheme.colors.background)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .appWidgetBackground()
            .cornerRadius(cornerRadius)
            .size(widgetMinSize)
            .applyRootBackground(legacyBackground, KeepOnWidgetColorScheme.colors.background)
            .padding(imagePadding),
    ) {
        CircularProgressIndicator(
            color = KeepOnWidgetColorScheme.colors.onBackground
        )
    }
}
