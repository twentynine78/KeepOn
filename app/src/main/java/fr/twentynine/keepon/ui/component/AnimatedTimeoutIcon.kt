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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import coil3.imageLoader
import coil3.request.SuccessResult
import coil3.toBitmap
import coil3.compose.AsyncImage
import fr.twentynine.keepon.core.coil.timeoutIconImageRequest
import fr.twentynine.keepon.core.transition.TransitionPlayer
import fr.twentynine.keepon.domain.catalog.IconTransitionCatalog
import fr.twentynine.keepon.domain.model.AffineTransition
import fr.twentynine.keepon.domain.model.FadingEdge
import fr.twentynine.keepon.domain.model.IconTransition
import fr.twentynine.keepon.domain.model.IconTransitionAnimation
import fr.twentynine.keepon.domain.model.IconTransitionTiming
import fr.twentynine.keepon.domain.model.RenderedTransition
import fr.twentynine.keepon.domain.model.ScreenTimeout
import fr.twentynine.keepon.domain.model.TimeoutIconData
import fr.twentynine.keepon.domain.model.TimeoutIconSize
import fr.twentynine.keepon.domain.model.TimeoutIconStyle
import fr.twentynine.keepon.ui.util.rememberTimeoutIconModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay

private const val FLIP_CAMERA_DISTANCE = 16f

private val EDGE_COLOR_STOPS = arrayOf(
    0f to Color.Transparent,
    FadingEdge.FADE_FRACTION to Color.Black,
    1f - FadingEdge.FADE_FRACTION to Color.Black,
    1f to Color.Transparent,
)

/**
 * The generated timeout icon with an optional change transition. When [animation] is enabled, a real
 * timeout change animates the previous and the new icon following its catalog entry (the same motion
 * the QS tile uses): an [AffineTransition] cross-animates the two layers (translation, scale, alpha
 * and a true 3D flip via `rotationX`) in Compose, while a [RenderedTransition] plays frames
 * composited by the shared renderer. Style changes and the first render update instantly.
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

    if (!animation.enabled) {
        TimeoutIcon(currentScreenTimeout, timeoutIconStyle, colorFilter, contentDescription, modifier.size(iconSize))
        return
    }

    val transition = remember(animation.typeId) { IconTransitionCatalog.fromId(animation.typeId) }
    val durationMs = remember(animation.durationStep) { IconTransitionTiming.durationMs(animation.durationStep) }

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
    // The edge fade is only meaningful while the icon is moving; gate it to the transition window so
    // a settled icon (centred glyph) is never clipped, which lets the fade be generous.
    var edgeFadeActive by remember { mutableStateOf(false) }
    var settledTimeout by remember { mutableStateOf(currentScreenTimeout) }
    LaunchedEffect(currentScreenTimeout) {
        if (currentScreenTimeout != settledTimeout) {
            edgeFadeActive = true
            delay(durationMs.toLong())
            edgeFadeActive = false
        }
        settledTimeout = currentScreenTimeout
    }

    val containerModifier = modifier
        .size(iconSize)
        .edgeFade(transition.fadingEdge, edgeFadeActive)

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
        modifier = containerModifier,
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

        val from = loadIconBitmap(context, previous, timeoutIconStyle)
        val to = loadIconBitmap(context, currentScreenTimeout, timeoutIconStyle)
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
private suspend fun loadIconBitmap(
    context: Context,
    screenTimeout: ScreenTimeout,
    timeoutIconStyle: TimeoutIconStyle,
): Bitmap? {
    val request = timeoutIconImageRequest(
        context,
        TimeoutIconData(screenTimeout, TimeoutIconSize.LARGE, timeoutIconStyle),
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
    val imageData = remember(screenTimeout, timeoutIconStyle) {
        TimeoutIconData(screenTimeout, TimeoutIconSize.LARGE, timeoutIconStyle)
    }
    AsyncImage(
        modifier = modifier,
        model = rememberTimeoutIconModel(imageData),
        colorFilter = colorFilter,
        contentDescription = contentDescription,
    )
}

/**
 * Fades the requested edges so sliding content appears/disappears softly. [active] gates the mask to
 * the transition window so a settled, centred glyph is never clipped. Supports vertical and/or
 * horizontal edges (the catalog only uses VERTICAL today; HORIZONTAL/BOTH are ready to use).
 */
private fun Modifier.edgeFade(fadingEdge: FadingEdge, active: Boolean): Modifier {
    if (fadingEdge == FadingEdge.NONE) return this
    return this
        .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
        .drawWithContent {
            drawContent()
            if (!active) return@drawWithContent
            if (fadingEdge.fadesVertical) {
                drawRect(brush = Brush.verticalGradient(*EDGE_COLOR_STOPS), blendMode = BlendMode.DstIn)
            }
            if (fadingEdge.fadesHorizontal) {
                drawRect(brush = Brush.horizontalGradient(*EDGE_COLOR_STOPS), blendMode = BlendMode.DstIn)
            }
        }
}
