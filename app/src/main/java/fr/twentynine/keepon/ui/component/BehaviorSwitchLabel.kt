package fr.twentynine.keepon.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import fr.twentynine.keepon.R

/**
 * The "reset on screen off" behavior label shown on the Home behavior switch, with the default
 * timeout [displayName] rendered in bold. Built by splitting the template on its single `%s` so the
 * inserted value carries the bold span. Shared so the Style transition switch can reserve the exact
 * same size for it as a ghost, keeping the two top switches aligned across screens.
 */
@Composable
fun rememberBehaviorSwitchLabel(displayName: String): AnnotatedString {
    val template = stringResource(R.string.general_behavior_short_text)
    return remember(template, displayName) {
        val parts = template.split("%s", limit = 2)
        buildAnnotatedString {
            append(parts[0])
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(displayName) }
            if (parts.size > 1) append(parts[1])
        }
    }
}
