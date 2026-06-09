package fr.twentynine.keepon.ui.widget.theme

import androidx.glance.material3.ColorProviders
import fr.twentynine.keepon.ui.theme.DarkColorScheme
import fr.twentynine.keepon.ui.theme.LightColorScheme

/**
 * The widget's color providers built from the app's light/dark schemes — the fallback used before
 * Glance's dynamic colors take over on Android 12+.
 */
object KeepOnWidgetColorScheme {
    val colors = ColorProviders(
        light = LightColorScheme,
        dark = DarkColorScheme
    )
}
