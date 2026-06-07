package fr.twentynine.keepon.ui.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * The timeout name shown in the home and Tasker rows, in the shared row text style, with a soft
 * glow when [showGlow] (the current timeout on the home screen, the selected one in Tasker).
 */
@Composable
fun TimeoutRowLabel(
    text: String,
    showGlow: Boolean,
    modifier: Modifier = Modifier,
) {
    GlowingText(
        modifier = modifier,
        style = MaterialTheme.typography.labelMedium,
        text = text,
        fontSize = 17.sp,
        fontWeight = FontWeight.SemiBold,
        showGlow = showGlow,
        glowColor = MaterialTheme.colorScheme.onSurface,
        glowRadius = 10.dp,
        glowSpread = 2.dp,
    )
}
