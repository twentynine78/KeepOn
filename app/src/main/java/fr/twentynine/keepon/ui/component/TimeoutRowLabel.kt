package fr.twentynine.keepon.ui.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp

/**
 * The timeout name shown in the home and Tasker rows, in the shared row text style.
 *
 * The overflow parameters default to the natural (unconstrained) behavior; callers that lay it out in
 * a width-bounded slot pass [maxLines] = 1, [softWrap] = false and [overflow] = Ellipsis to make it
 * ellipsize instead of overflowing on narrow screens.
 */
@Composable
fun TimeoutRowLabel(
    text: String,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE,
    softWrap: Boolean = true,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurface,
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold,
        maxLines = maxLines,
        softWrap = softWrap,
        overflow = overflow,
    )
}
