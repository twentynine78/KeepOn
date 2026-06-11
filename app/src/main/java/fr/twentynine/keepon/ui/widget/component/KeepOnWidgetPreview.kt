package fr.twentynine.keepon.ui.widget.component

import android.content.Context
import android.graphics.Bitmap
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
 * The app icon inset by 10dp, used as the stand-in for the generated timeout icon wherever there is
 * no live Coil pipeline (the launcher widget-picker preview and the Android Studio previews).
 */
internal fun widgetPlaceholderBitmap(context: Context): Bitmap? {
    val paddingInPixels = (10 * context.resources.displayMetrics.density).toInt()
    return ResourcesCompat.getDrawable(context.resources, R.drawable.ic_keepon, context.theme)
        ?.let { InsetDrawable(it, paddingInPixels) }
        ?.toBitmap()
}

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
                val bitmap = widgetPlaceholderBitmap(LocalContext.current)

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
