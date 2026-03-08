package fr.twentynine.keepon.ui.widget

import android.graphics.drawable.InsetDrawable
import androidx.compose.runtime.Composable
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.glance.ColorFilter
import androidx.glance.LocalContext
import fr.twentynine.keepon.R
import fr.twentynine.keepon.data.model.WidgetUIState

@Composable
fun KeepOnWidgetPreview(
    widgetUIState: WidgetUIState,
) {
    KeepOnWidgetTheme(widgetUIState) { currentState ->
        when (currentState) {
            is WidgetUIState.Loading -> KeepOnWidgetLoading()
            is WidgetUIState.Success -> {
                // Get local context
                val context = LocalContext.current
                // Get colors
                val borderColor = KeepOnWidgetColorScheme.colors.primaryContainer.getColor(context)
                val widgetBackgroundColor = KeepOnWidgetColorScheme.colors.widgetBackground.getColor(context)
                    .copy(alpha = WIDGET_BACKGROUND_COLOR_ALPHA)
                val backgroundColor = KeepOnWidgetColorScheme.colors.background
                val imageColorFilter = ColorFilter.tint(KeepOnWidgetColorScheme.colors.onBackground)
                val contentColor = KeepOnWidgetColorScheme.colors.onBackground

                // Get the bitmap from drawable icon with 10.dp padding
                val localContext = LocalContext.current
                val resources = localContext.resources
                val paddingInPixels = (10 * resources.displayMetrics.density).toInt()
                val bitmap = ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.ic_keepon,
                    localContext.theme
                )?.let {
                    InsetDrawable(it, paddingInPixels)
                }?.toBitmap()

                KeepOnWidgetContent(
                    borderColor,
                    backgroundColor,
                    widgetBackgroundColor,
                    imageColorFilter,
                    contentColor,
                    bitmap,
                    null
                )
            }

            is WidgetUIState.Error -> KeepOnWidgetError(currentState)
        }
    }
}
