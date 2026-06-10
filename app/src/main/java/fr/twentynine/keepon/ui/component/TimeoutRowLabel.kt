package fr.twentynine.keepon.ui.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * The timeout name shown in the home and Tasker rows, in the shared row text style.
 */
@Composable
fun TimeoutRowLabel(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.titleSmall,
        fontSize = 17.sp,
        fontWeight = FontWeight.SemiBold,
    )
}
