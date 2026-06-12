package fr.twentynine.keepon.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import fr.twentynine.keepon.R
import fr.twentynine.keepon.ui.model.ScreenTimeoutUI

private const val CONTENT_COLOR_ANIMATION_DURATION_MS = 200
private const val BACKGROUND_COLOR_ANIMATION_DURATION_MS = 400

/**
 * The swipe background specialized for the "set as default" gesture on a timeout row: it animates its
 * colors/label by [dismissProgress] and shows whether the swipe will set this timeout as the default
 * or is a no-op (already default / not swipeable).
 */
@Composable
fun SetDefaultDismissActionRow(
    dismissDirection: SwipeToDismissBoxValue,
    dismissProgress: Float,
    screenTimeoutUI: ScreenTimeoutUI,
    swipeEnabledState: Boolean,
    swipeThresholdFraction: Float,
) {
    val backgroundEnabledColor = MaterialTheme.colorScheme.secondaryContainer
    val backgroundDisabledColor = MaterialTheme.colorScheme.outlineVariant
    val contentEnabledTint = MaterialTheme.colorScheme.onSecondaryContainer
    val contentDisabledTint = MaterialTheme.colorScheme.onSurfaceVariant

    val enabledText = stringResource(R.string.select_timeouts_swipe_set_default_text)
    val disabledText = stringResource(R.string.select_timeouts_swipe_already_default_text)

    val textSetDefault = if (!screenTimeoutUI.isDefault) enabledText else disabledText
    val iconSetDefault = if (!screenTimeoutUI.isDefault) Icons.Rounded.PushPin else Icons.Rounded.Done
    val currentContentTintForAnimation = if (!screenTimeoutUI.isDefault) contentEnabledTint else contentDisabledTint
    val currentBackgroundColorForAnimation = when {
        !swipeEnabledState -> Color.Transparent
        screenTimeoutUI.isDefault -> backgroundDisabledColor
        else -> backgroundEnabledColor
    }

    val animatedBackgroundColor by animateColorAsState(
        targetValue = currentBackgroundColorForAnimation,
        animationSpec = tween(durationMillis = BACKGROUND_COLOR_ANIMATION_DURATION_MS),
        label = "backgroundColorAnimation"
    )

    val animatedContentColor by animateColorAsState(
        targetValue = currentContentTintForAnimation.copy(
            alpha = if (!screenTimeoutUI.isDefault && dismissProgress < swipeThresholdFraction && dismissProgress != 0f) 0.5f else 1f
        ),
        animationSpec = tween(durationMillis = CONTENT_COLOR_ANIMATION_DURATION_MS),
        label = "contentColorAnimation"
    )

    val arrangement = when (dismissDirection) {
        SwipeToDismissBoxValue.EndToStart -> androidx.compose.foundation.layout.Arrangement.End
        SwipeToDismissBoxValue.StartToEnd -> androidx.compose.foundation.layout.Arrangement.Start
        else -> androidx.compose.foundation.layout.Arrangement.Start
    }

    DismissActionRow(
        icon = iconSetDefault,
        text = textSetDefault,
        contentColor = animatedContentColor,
        backgroundColor = animatedBackgroundColor,
        horizontalArrangement = arrangement,
        contentVisible = swipeEnabledState
    )
}
