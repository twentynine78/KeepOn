package fr.twentynine.keepon.ui.widget.component

import android.graphics.drawable.InsetDrawable
import androidx.compose.runtime.Composable
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.glance.LocalContext
import fr.twentynine.keepon.R
import fr.twentynine.keepon.ui.state.WidgetUIState
import fr.twentynine.keepon.ui.widget.theme.KeepOnWidgetTheme
import fr.twentynine.keepon.ui.widget.theme.rememberWidgetColors

/**
 * The static widget preview shown in the launcher's widget picker: the same layout as the live widget
 * but using the app icon as a stand-in bitmap and the inactive palette (there's no live state to drive
 * it), and with no click action.
 */
@Composable
fun KeepOnWidgetPreview(
    widgetUIState: WidgetUIState,
) {
    KeepOnWidgetTheme(widgetUIState) { currentState ->
        when (currentState) {
            is WidgetUIState.Loading -> KeepOnWidgetLoading()
            is WidgetUIState.Success -> {
                // The preview shows the inactive palette (no live state to drive it).
                val colors = rememberWidgetColors(keepOnIsActive = false)

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
                    borderColor = colors.borderColor,
                    backgroundColor = colors.backgroundColor,
                    widgetBackgroundColor = colors.widgetBackgroundColor,
                    imageColorFilter = colors.imageColorFilter,
                    contentColor = colors.contentColor,
                    contentBitmap = bitmap,
                    onClickAction = null,
                )
            }

            is WidgetUIState.Error -> KeepOnWidgetError(currentState)
        }
    }
}
