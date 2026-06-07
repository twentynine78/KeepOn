package fr.twentynine.keepon.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import coil3.request.ImageRequest
import fr.twentynine.keepon.core.coil.timeoutIconImageRequest
import fr.twentynine.keepon.domain.model.TimeoutIconData

/**
 * Remembers the Coil model (an [ImageRequest]) for a generated timeout icon so all
 * call sites share the same size-independent request building.
 */
@Composable
fun rememberTimeoutIconModel(data: TimeoutIconData): ImageRequest {
    val context = LocalContext.current
    return remember(data) { timeoutIconImageRequest(context, data) }
}
