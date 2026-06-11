package fr.twentynine.keepon.ui.component

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import coil3.imageLoader
import coil3.request.SuccessResult
import coil3.toBitmap
import fr.twentynine.keepon.core.coil.timeoutIconImageRequest
import fr.twentynine.keepon.core.transition.TransitionPlayer
import fr.twentynine.keepon.domain.catalog.IconTransitionCatalog
import fr.twentynine.keepon.domain.model.AffineTransition
import fr.twentynine.keepon.domain.model.IconTransition
import fr.twentynine.keepon.domain.model.IconTransitionAnimation
import fr.twentynine.keepon.domain.model.IconTransitionTiming
import fr.twentynine.keepon.domain.model.RenderedTransition
import fr.twentynine.keepon.domain.model.ScreenTimeout
import fr.twentynine.keepon.domain.model.TimeoutIconData
import fr.twentynine.keepon.domain.model.TimeoutIconSize
import fr.twentynine.keepon.domain.model.TimeoutIconStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

private const val FLIP_CAMERA_DISTANCE = 16f

/**
 * The generated timeout icon with an optional change transition. When [animation] is enabled, a real
 * timeout change animates the previous and the new icon following its catalog entry (the same motion
 * the QS tile uses): an [AffineTransition] cross-animates the two layers (translation, scale, alpha
 * and a true 3D flip via `rotationX`) in Compose, while a [RenderedTransition] plays frames
 * composited by the shared renderer. Style changes and the first render update instantly. (The
 * settings preview of an effect lives in the Style screen's animation grid, not here.)
 */
@Composable
fun AnimatedTimeoutIcon(
    currentScreenTimeout: ScreenTimeout,
    timeoutIconStyle: TimeoutIconStyle,
    animation: IconTransitionAnimation,
    tint: Color,
    contentDescription: String,
    iconSize: Dp,
    modifier: Modifier = Modifier,
) {
    val colorFilter = remember(tint) { ColorFilter.tint(tint) }
    val transition = remember(animation.typeId) { IconTransitionCatalog.fromId(animation.typeId) }
    val durationMs = remember(animation.durationStep) { IconTransitionTiming.durationMs(animation.durationStep) }

    if (!animation.enabled) {
        TimeoutIcon(currentScreenTimeout, timeoutIconStyle, colorFilter, contentDescription, modifier.size(iconSize))
        return
    }

    when (transition) {
        is AffineTransition -> AffineTimeoutIcon(
            currentScreenTimeout, timeoutIconStyle, transition, durationMs, colorFilter, contentDescription, iconSize, modifier,
        )
        is RenderedTransition -> RenderedFrameTimeoutIcon(
            currentScreenTimeout, timeoutIconStyle, transition, durationMs, colorFilter, contentDescription, iconSize, modifier,
        )
    }
}

@Composable
private fun AffineTimeoutIcon(
    currentScreenTimeout: ScreenTimeout,
    timeoutIconStyle: TimeoutIconStyle,
    transition: AffineTransition,
    durationMs: Int,
    colorFilter: ColorFilter,
    contentDescription: String,
    iconSize: Dp,
    modifier: Modifier,
) {
    AnimatedContent(
        targetState = currentScreenTimeout,
        // No-op fade just keeps both contents alive for the duration; the real motion is the
        // per-layer graphicsLayer below (covers scale + rotationX, which built-ins can't).
        transitionSpec = {
            (
                fadeIn(tween(durationMs), initialAlpha = 1f) togetherWith
                    fadeOut(tween(durationMs), targetAlpha = 1f)
                ) using SizeTransform(clip = false)
        },
        label = "timeoutIconTransition",
        modifier = modifier.size(iconSize),
    ) { timeout ->
        val entering = this.transition.targetState == EnterExitState.Visible
        val presence = this.transition.animateFloat(
            transitionSpec = {
                val toVisible = targetState == EnterExitState.Visible
                tween(
                    durationMillis = if (transition.sequential) durationMs / 2 else durationMs,
                    delayMillis = if (transition.sequential && toVisible) durationMs / 2 else 0,
                    easing = FastOutSlowInEasing,
                )
            },
            label = "presence",
        ) { state -> if (state == EnterExitState.Visible) 1f else 0f }

        TimeoutIcon(
            screenTimeout = timeout,
            timeoutIconStyle = timeoutIconStyle,
            colorFilter = colorFilter,
            contentDescription = contentDescription,
            modifier = Modifier
                .size(iconSize)
                .graphicsLayer {
                    val layer = transition.transform(entering, presence.value)
                    translationX = layer.translationXFraction * size.width
                    translationY = layer.translationYFraction * size.height
                    scaleX = layer.scaleX
                    scaleY = layer.scaleY
                    rotationX = layer.rotationX
                    alpha = layer.alpha
                    cameraDistance = FLIP_CAMERA_DISTANCE * density
                },
        )
    }
}

/**
 * Plays a renderer-composited transition (e.g. morph/warp/particle) on the FAB. The frames are
 * composited off the main thread by the shared transition renderer (the same motion the tile uses),
 * then shown in sequence; a settled icon falls back to the crisp generated
 * bitmap. Only a real timeout change animates — the first render and style-only changes update
 * instantly.
 */
@Composable
private fun RenderedFrameTimeoutIcon(
    currentScreenTimeout: ScreenTimeout,
    timeoutIconStyle: TimeoutIconStyle,
    transition: IconTransition,
    durationMs: Int,
    colorFilter: ColorFilter,
    contentDescription: String,
    iconSize: Dp,
    modifier: Modifier,
) {
    val context = LocalContext.current
    var morphFrame by remember { mutableStateOf<Bitmap?>(null) }
    // The icon the still (non-animating) fallback shows. It lags currentScreenTimeout until the
    // animation's first frame is ready, so the crisp target never flashes up before the transition.
    var shownTimeout by remember { mutableStateOf(currentScreenTimeout) }

    LaunchedEffect(currentScreenTimeout, timeoutIconStyle) {
        val previous = shownTimeout
        if (previous == currentScreenTimeout) {
            // First render or a pure style change: show the crisp icon, never animate.
            morphFrame = null
            return@LaunchedEffect
        }

        // Load both icons concurrently so the animation starts as soon as the slower one is ready.
        // The previous icon is usually cached and the incoming one is prefetched (PrefetchTimeoutIcons),
        // so this is normally two cache hits, but parallelising removes the sequential dependency.
        val (from, to) = coroutineScope {
            val fromDeferred = async { loadIconBitmap(context, previous, timeoutIconStyle) }
            val toDeferred = async { loadIconBitmap(context, currentScreenTimeout, timeoutIconStyle) }
            fromDeferred.await() to toDeferred.await()
        }
        if (from == null || to == null) {
            morphFrame = null
            shownTimeout = currentScreenTimeout
            return@LaunchedEffect
        }

        // Composite each frame off the main thread (the LaunchedEffect runs on it); the tile and
        // widget already collect on a background dispatcher and composite in place.
        TransitionPlayer.play(
            transition = transition,
            from = from,
            to = to,
            durationMs = durationMs,
            maxFrames = IconTransitionTiming.FRAME_COUNT,
            renderContext = Dispatchers.Default,
        ) { frame -> morphFrame = frame }
        morphFrame = null
        // Switch the fallback to the new icon only now — until the first frame was composited the
        // fallback kept showing the previous icon, so the crisp target never flashed up front.
        shownTimeout = currentScreenTimeout
    }

    val frame = morphFrame
    if (frame != null) {
        Image(
            bitmap = frame.asImageBitmap(),
            contentDescription = contentDescription,
            colorFilter = colorFilter,
            modifier = modifier.size(iconSize),
        )
    } else {
        TimeoutIcon(shownTimeout, timeoutIconStyle, colorFilter, contentDescription, modifier.size(iconSize))
    }
}

/** Loads the generated timeout-icon bitmap through Coil (served from cache once it has been shown). */
internal suspend fun loadIconBitmap(
    context: Context,
    screenTimeout: ScreenTimeout,
    timeoutIconStyle: TimeoutIconStyle,
    iconSize: TimeoutIconSize = TimeoutIconSize.LARGE,
): Bitmap? {
    val request = timeoutIconImageRequest(
        context,
        TimeoutIconData(screenTimeout, iconSize, timeoutIconStyle),
    )
    return (context.imageLoader.execute(request) as? SuccessResult)?.image?.toBitmap()
}

@Composable
private fun TimeoutIcon(
    screenTimeout: ScreenTimeout,
    timeoutIconStyle: TimeoutIconStyle,
    colorFilter: ColorFilter,
    contentDescription: String,
    modifier: Modifier,
) {
    // LARGE: this icon is only shown on the FAB (40dp), where the MEDIUM scale would be upscaled.
    val imageData = remember(screenTimeout, timeoutIconStyle) {
        TimeoutIconData(screenTimeout, TimeoutIconSize.LARGE, timeoutIconStyle)
    }
    TimeoutIconAsyncImage(
        data = imageData,
        contentDescription = contentDescription,
        colorFilter = colorFilter,
        modifier = modifier,
    )
}
