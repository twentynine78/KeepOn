package fr.twentynine.keepon.ui.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight


/**
 * A subsection label inside a settings card (e.g. "Animation type", "Animation duration"). Bold and
 * tinted with the accent color so it reads as a heading above its controls, a clear tier between the
 * card header and the plain option labels.
 */
@Composable
fun Subtitle(text: String, modifier: Modifier = Modifier) {
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val primaryColor = MaterialTheme.colorScheme.primary
    // Theme-derived, so cache it: a slider's value Subtitle recomposes repeatedly while dragging.
    val accentColor = remember(onSurfaceColor, primaryColor) {
        lerp(start = onSurfaceColor, stop = primaryColor, fraction = 0.6f)
    }
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.Bold,
        color = accentColor,
        modifier = modifier,
    )
}