package fr.twentynine.keepon.ui.util

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.material3.BottomAppBarState
import androidx.compose.material3.ExperimentalMaterial3Api

/**
 * Re-deploys the bottom app bar by animating its height offset back to 0 (the bar slides up into
 * view). A no-op when the bar is already fully visible, so it can be triggered unconditionally on
 * reaching a destination without producing a spurious animation.
 */
@OptIn(ExperimentalMaterial3Api::class)
suspend fun BottomAppBarState.expand(
    animationSpec: AnimationSpec<Float> = tween(durationMillis = 300, easing = FastOutSlowInEasing),
) {
    if (heightOffset == 0f) return
    animate(
        initialValue = heightOffset,
        targetValue = 0f,
        animationSpec = animationSpec,
    ) { value, _ -> heightOffset = value }
}
