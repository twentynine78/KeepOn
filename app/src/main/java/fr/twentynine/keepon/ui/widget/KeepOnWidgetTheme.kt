package fr.twentynine.keepon.ui.widget

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.glance.GlanceTheme
import fr.twentynine.keepon.data.model.WidgetUIState

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
