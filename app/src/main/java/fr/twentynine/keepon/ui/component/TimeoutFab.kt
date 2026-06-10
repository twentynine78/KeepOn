package fr.twentynine.keepon.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.FloatingActionButtonElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.twentynine.keepon.domain.model.IconTransitionAnimation
import fr.twentynine.keepon.domain.model.ScreenTimeout
import fr.twentynine.keepon.domain.model.TimeoutIconStyle
import fr.twentynine.keepon.ui.theme.KeepOnCardCornerRadius

private val FabShape = RoundedCornerShape(KeepOnCardCornerRadius)
private val FabSize = 68.dp
private val FabIconSize = 40.dp
private const val FAB_DEFAULT_ANIMATION_DURATION_MS = 50

/**
 * The current-timeout floating action button: shows the generated timeout icon and animates its
 * colors with the active state. Shared by the bottom-navigation Scaffold and the navigation rail.
 */
@Composable
fun TimeoutFab(
    keepOnIsActive: Boolean,
    currentScreenTimeout: ScreenTimeout,
    currentTimeoutDisplay: String,
    timeoutIconStyle: TimeoutIconStyle,
    iconTransitionAnimation: IconTransitionAnimation,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    animationDurationMs: Int = FAB_DEFAULT_ANIMATION_DURATION_MS,
    elevation: FloatingActionButtonElevation = FloatingActionButtonDefaults.elevation(),
) {
    val colorScheme = MaterialTheme.colorScheme
    val fabBackgroundColor by animateColorAsState(
        targetValue = if (keepOnIsActive) colorScheme.primaryContainer else colorScheme.background,
        animationSpec = tween(animationDurationMs),
        label = "fabBackgroundColor",
    )
    val fabBorderColor by animateColorAsState(
        targetValue = if (keepOnIsActive) colorScheme.background else colorScheme.primaryContainer,
        animationSpec = tween(animationDurationMs),
        label = "fabBorderColor",
    )
    val fabContentColor by animateColorAsState(
        targetValue = if (keepOnIsActive) colorScheme.onPrimaryContainer else colorScheme.onBackground,
        animationSpec = tween(animationDurationMs),
        label = "fabContentColor",
    )

    FloatingActionButton(
        modifier = modifier
            .border(width = 1.dp, color = fabBorderColor, shape = FabShape)
            .size(FabSize),
        onClick = onClick,
        containerColor = fabBackgroundColor,
        contentColor = fabContentColor,
        elevation = elevation,
        shape = FabShape,
    ) {
        AnimatedTimeoutIcon(
            currentScreenTimeout = currentScreenTimeout,
            timeoutIconStyle = timeoutIconStyle,
            animation = iconTransitionAnimation,
            tint = fabContentColor,
            contentDescription = currentTimeoutDisplay,
            iconSize = FabIconSize,
            modifier = Modifier.padding(bottom = 2.dp),
        )
    }
}
