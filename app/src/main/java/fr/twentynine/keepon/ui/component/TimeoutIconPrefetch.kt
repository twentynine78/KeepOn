package fr.twentynine.keepon.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import coil3.imageLoader
import fr.twentynine.keepon.core.coil.timeoutIconImageRequest
import fr.twentynine.keepon.domain.model.ScreenTimeout
import fr.twentynine.keepon.domain.model.TimeoutIconData
import fr.twentynine.keepon.domain.model.TimeoutIconSize
import fr.twentynine.keepon.domain.model.TimeoutIconStyle

/**
 * Warms Coil's memory cache with the LARGE timeout icons the FAB can switch to, so a tap animates
 * immediately instead of stalling while the incoming icon is generated on first use. The list chips
 * render at MEDIUM, so the LARGE entries (used by the FAB and the still fallback) would otherwise be
 * a cache miss on the first transition to each timeout. Re-runs when the set or the style changes.
 */
@Composable
fun PrefetchTimeoutIcons(timeouts: List<ScreenTimeout>, timeoutIconStyle: TimeoutIconStyle) {
    val context = LocalContext.current
    LaunchedEffect(timeouts, timeoutIconStyle) {
        val loader = context.imageLoader
        timeouts.forEach { timeout ->
            loader.enqueue(
                timeoutIconImageRequest(
                    context,
                    TimeoutIconData(timeout, TimeoutIconSize.LARGE, timeoutIconStyle),
                ),
            )
        }
    }
}
