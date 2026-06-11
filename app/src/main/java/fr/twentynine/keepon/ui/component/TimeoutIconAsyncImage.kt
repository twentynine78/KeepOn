package fr.twentynine.keepon.ui.component

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import fr.twentynine.keepon.domain.model.TimeoutIconData
import fr.twentynine.keepon.ui.util.rememberTimeoutIconModel

// The icon is generated in memory, so a failure is transient and retrying is cheap; the cap
// guards against looping on a request that genuinely cannot succeed.
private const val MAX_RETRY_ATTEMPTS = 2

/**
 * [Image] backed by a generated timeout icon, self-healing on load failure: Coil's Error state is
 * terminal (no auto-retry) and these icons render without an error placeholder, so a transient
 * generation failure would otherwise stay blank for as long as the composable remains composed —
 * e.g. a Home row that never leaves the composition once scrolled in. On Error the request is
 * relaunched, capped at [MAX_RETRY_ATTEMPTS] per model.
 */
@Composable
fun TimeoutIconAsyncImage(
    data: TimeoutIconData,
    contentDescription: String?,
    colorFilter: ColorFilter?,
    modifier: Modifier = Modifier,
) {
    val model = rememberTimeoutIconModel(data)
    val painter = rememberAsyncImagePainter(model)

    LaunchedEffect(painter, model) {
        var attempts = 0
        painter.state.collect { state ->
            if (state is AsyncImagePainter.State.Error && attempts < MAX_RETRY_ATTEMPTS) {
                attempts++
                painter.restart()
            }
        }
    }

    Image(
        painter = painter,
        contentDescription = contentDescription,
        colorFilter = colorFilter,
        modifier = modifier,
    )
}
