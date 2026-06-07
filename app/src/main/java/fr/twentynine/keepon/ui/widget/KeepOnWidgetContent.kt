package fr.twentynine.keepon.ui.widget

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

@Composable
fun KeepOnWidgetContent(
    borderColor: Color,
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

    Box(
        contentAlignment = Alignment.Center,
        modifier = GlanceModifier
            .appWidgetBackground()
            .cornerRadius(cornerRadius + (outerBoxPadding * 2))
            .size(widgetMinSize + (outerBoxPadding * 2))
            .background(widgetBackgroundColor)
            .padding(outerBoxPadding),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = GlanceModifier
                .cornerRadius(cornerRadius)
                .size(widgetMinSize)
                .background(borderColor)
                .padding(borderSize),
        ) {
            val contentWidgetModifier = GlanceModifier
                .cornerRadius(cornerRadius)
                .fillMaxSize()
                .background(backgroundColor)

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
