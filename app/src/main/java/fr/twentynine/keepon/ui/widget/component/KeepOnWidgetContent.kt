package fr.twentynine.keepon.ui.widget.component

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.CircularProgressIndicator
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.unit.ColorProvider
import fr.twentynine.keepon.ui.widget.theme.applyLayerBackground
import fr.twentynine.keepon.ui.widget.theme.applyRootBackground
import fr.twentynine.keepon.ui.widget.theme.rememberLegacyWidgetBackground
import fr.twentynine.keepon.ui.widget.theme.rememberWidgetDimens

/**
 * The widget's success layout: the generated timeout-icon [contentBitmap] centered on the themed,
 * rounded widget background, tinted by the active/inactive palette and clickable via [onClickAction]
 * (falls back to a progress indicator while the bitmap is null).
 */
@Composable
fun KeepOnWidgetContent(
    borderColor: ColorProvider,
    backgroundColor: ColorProvider,
    widgetBackgroundColor: Color,
    imageColorFilter: ColorFilter,
    contentColor: ColorProvider,
    contentBitmap: Bitmap?,
    onClickAction: Action?,
) {
    val dimens = rememberWidgetDimens()
    val widgetMinSize = dimens.widgetMinSize
    val cornerRadius = dimens.cornerRadius
    val outerBoxPadding = dimens.outerBoxPadding
    val borderSize = dimens.borderSize
    val imagePadding = dimens.imagePadding

    // On API < 31 cornerRadius() is a no-op; the rounded look comes from a single pre-rendered
    // bitmap of the three concentric circles set as the root background (null on API 31+).
    val legacyBackground = rememberLegacyWidgetBackground(borderColor, backgroundColor, widgetBackgroundColor)

    Box(
        contentAlignment = Alignment.Center,
        modifier = GlanceModifier
            .appWidgetBackground()
            .cornerRadius(cornerRadius + (outerBoxPadding * 2))
            .size(widgetMinSize + (outerBoxPadding * 2))
            .applyRootBackground(legacyBackground, widgetBackgroundColor)
            .padding(outerBoxPadding),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = GlanceModifier
                .cornerRadius(cornerRadius)
                .size(widgetMinSize)
                .applyLayerBackground(legacyBackground, borderColor)
                .padding(borderSize),
        ) {
            val contentWidgetModifier = GlanceModifier
                .cornerRadius(cornerRadius)
                .fillMaxSize()
                .applyLayerBackground(legacyBackground, backgroundColor)

            val widgetContentModifier = if (onClickAction != null) {
                contentWidgetModifier
                    .clickable(onClickAction)
            } else {
                contentWidgetModifier
            }

            Box(
                modifier = widgetContentModifier,
                contentAlignment = Alignment.Center,
            ) {
                contentBitmap?.let {
                    Image(
                        provider = ImageProvider(contentBitmap),
                        contentDescription = null,
                        modifier = GlanceModifier
                            .fillMaxSize()
                            .background(Color.Transparent)
                            .padding(imagePadding),
                        colorFilter = imageColorFilter,
                    )
                } ?: run {
                    CircularProgressIndicator(
                        modifier = GlanceModifier
                            .padding(imagePadding),
                        color = contentColor,
                    )
                }
            }
        }
    }
}
