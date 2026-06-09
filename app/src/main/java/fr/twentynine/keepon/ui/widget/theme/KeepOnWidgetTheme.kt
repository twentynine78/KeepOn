package fr.twentynine.keepon.ui.widget.theme

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.glance.GlanceTheme
import fr.twentynine.keepon.ui.state.WidgetUIState

/**
 * Wraps the widget [content] in a [GlanceTheme]: dynamic system colors on Android 12+, falling back
 * to [KeepOnWidgetColorScheme] on older versions.
 */
@Composable
fun KeepOnWidgetTheme(
    widgetUIState: WidgetUIState,
    content: @Composable (WidgetUIState) -> Unit,
) {
    GlanceTheme(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            GlanceTheme.colors
        } else {
            KeepOnWidgetColorScheme.colors
        }
    ) {
        content.invoke(widgetUIState)
    }
}
