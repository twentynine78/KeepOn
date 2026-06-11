package fr.twentynine.keepon.ui.widget.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import fr.twentynine.keepon.ui.widget.theme.KeepOnWidgetColorScheme
import fr.twentynine.keepon.ui.widget.theme.rememberWidgetColors

// Design-time previews of the Glance widget for Android Studio. The generated timeout icon needs the
// Coil pipeline at runtime, so these use the app icon as a placeholder bitmap — the previews are for
// checking the layout, colors, rounded corners and the active/inactive palettes.

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 80, heightDp = 80)
@Composable
private fun KeepOnWidgetActivePreview() {
    WidgetContentPreview(keepOnIsActive = true)
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 80, heightDp = 80)
@Composable
private fun KeepOnWidgetInactivePreview() {
    WidgetContentPreview(keepOnIsActive = false)
}

@Composable
private fun WidgetContentPreview(keepOnIsActive: Boolean) {
    val context = LocalContext.current
    val colors = rememberWidgetColors(keepOnIsActive)
    val placeholderBitmap = remember { widgetPlaceholderBitmap(context) }

    GlanceTheme(KeepOnWidgetColorScheme.colors) {
        KeepOnWidgetContent(
            borderColor = colors.borderColor,
            backgroundColor = colors.backgroundColor,
            widgetBackgroundColor = colors.widgetBackgroundColor,
            imageColorFilter = colors.imageColorFilter,
            contentColor = colors.contentColor,
            contentBitmap = placeholderBitmap,
            onClickAction = null,
        )
    }
}
