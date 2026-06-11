package fr.twentynine.keepon.ui.component

import androidx.compose.animation.core.InfiniteTransition
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * An icon that continuously pulses (scales up and down) driven by the supplied [infiniteTransition],
 * so several pulsing elements can share one clock. Used to draw the eye to call-to-action icons.
 */
@Composable
fun PulsatingIcon(
    infiniteTransition: InfiniteTransition,
    initialSize: Float,
    imageVector: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    // Pulse with a graphicsLayer scale (draw-time only) instead of animating the size, which
    // would re-run layout on every frame.
    val pulsate by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.49f,
        animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse)
    )
    Box(
        modifier = Modifier
            .size((initialSize * 1.5f).dp)
            .padding(bottom = 2.dp),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            modifier = modifier
                .size(initialSize.dp)
                .graphicsLayer {
                    scaleX = pulsate
                    scaleY = pulsate
                }
        )
    }
}
