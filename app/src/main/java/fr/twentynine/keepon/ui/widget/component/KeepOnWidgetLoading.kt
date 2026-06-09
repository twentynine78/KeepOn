package fr.twentynine.keepon.ui.widget.component

import androidx.compose.runtime.Composable
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.CircularProgressIndicator
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.padding
import androidx.glance.layout.size
import fr.twentynine.keepon.ui.widget.theme.KeepOnWidgetColorScheme
import fr.twentynine.keepon.ui.widget.theme.applyRootBackground
import fr.twentynine.keepon.ui.widget.theme.rememberLegacyCircleBackground
import fr.twentynine.keepon.ui.widget.theme.rememberWidgetDimens

/** The widget's loading state: a centered progress indicator on the rounded widget background. */
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
