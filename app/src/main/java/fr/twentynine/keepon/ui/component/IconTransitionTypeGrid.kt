package fr.twentynine.keepon.ui.component

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import fr.twentynine.keepon.core.transition.TransitionPlayer
import fr.twentynine.keepon.domain.catalog.IconTransitionCatalog
import fr.twentynine.keepon.domain.model.IconTransition
import fr.twentynine.keepon.domain.model.IconTransitionAnimation
import fr.twentynine.keepon.domain.model.IconTransitionTiming
import fr.twentynine.keepon.domain.model.ScreenTimeout
import fr.twentynine.keepon.domain.model.TimeoutIconStyle
import fr.twentynine.keepon.ui.catalog.IconTransitionGlyphCatalog
import fr.twentynine.keepon.ui.model.IconTransitionOptionUI
import fr.twentynine.keepon.ui.theme.CHIP_BACKGROUND_ALPHA
import fr.twentynine.keepon.ui.theme.CHIP_BORDER_ALPHA
import fr.twentynine.keepon.ui.theme.KeepOnChipShape
import fr.twentynine.keepon.ui.theme.KeepOnTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

private val GridCellSpacing = 8.dp
private val GridMinCellWidth = 104.dp
private val TileShape = RoundedCornerShape(16.dp)
private val TilePadding = 10.dp
private val TileIconChipSize = 44.dp
private val TileIconSize = 24.dp
private val TileLabelSpacing = 6.dp
private val TileBadgePadding = 6.dp
private val TileBadgeSize = 18.dp
private val TileBadgeIconSize = 12.dp
private const val TILE_COLOR_ANIMATION_MS = 200

// Coalesces the rapid config changes of dragging the duration slider into a single preview play.
private const val PREVIEW_DEBOUNCE_MS = 150L

/**
 * The animation-type picker of the Style screen: a single-select grid of tiles, one per catalog
 * transition, each showing its glyph in a centered chip with the localized label below. The selected
 * tile is highlighted (primary-container fill, primary border and chip, corner checkmark badge) and
 * plays the effect's preview in its chip: a double pass from the tile's glyph to the current timeout
 * icon and back, rendered by the shared transition engine (replayed on selection, on re-tap, on a
 * duration change and on enable).
 *
 * Responsive: the column count adapts to the available width (at least two columns), so the grid
 * stays clean from narrow phones to tablets.
 */
@Composable
fun IconTransitionTypeGrid(
    options: List<IconTransitionOptionUI>,
    animation: IconTransitionAnimation,
    currentScreenTimeout: ScreenTimeout,
    timeoutIconStyle: TimeoutIconStyle,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Bumped when the already-selected tile is re-tapped, so its preview replays.
    var replayTick by remember { mutableIntStateOf(0) }

    BoxWithConstraints(modifier = modifier) {
        val columns = (maxWidth / GridMinCellWidth).toInt().coerceIn(2, maxOf(options.size, 2))

        Column(
            modifier = Modifier.selectableGroup(),
            verticalArrangement = Arrangement.spacedBy(GridCellSpacing),
        ) {
            options.chunked(columns).forEach { rowOptions ->
                Row(horizontalArrangement = Arrangement.spacedBy(GridCellSpacing)) {
                    rowOptions.forEach { option ->
                        val selected = option.id == animation.typeId
                        IconTransitionTile(
                            option = option,
                            selected = selected,
                            enabled = animation.enabled,
                            durationStep = animation.durationStep,
                            replayTick = replayTick,
                            currentScreenTimeout = currentScreenTimeout,
                            timeoutIconStyle = timeoutIconStyle,
                            onClick = { if (selected) replayTick++ else onSelect(option.id) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    repeat(columns - rowOptions.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun IconTransitionTile(
    option: IconTransitionOptionUI,
    selected: Boolean,
    enabled: Boolean,
    durationStep: Int,
    replayTick: Int,
    currentScreenTimeout: ScreenTimeout,
    timeoutIconStyle: TimeoutIconStyle,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val containerColor by animateColorAsState(
        targetValue = if (selected) {
            colorScheme.primaryContainer
        } else {
            colorScheme.surface.copy(alpha = CHIP_BACKGROUND_ALPHA)
        },
        animationSpec = tween(TILE_COLOR_ANIMATION_MS),
        label = "TileContainerColor",
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) {
            colorScheme.primary.copy(alpha = CHIP_BORDER_ALPHA)
        } else {
            colorScheme.outline.copy(alpha = CHIP_BORDER_ALPHA)
        },
        animationSpec = tween(TILE_COLOR_ANIMATION_MS),
        label = "TileBorderColor",
    )
    val labelColor by animateColorAsState(
        targetValue = if (selected) colorScheme.onPrimaryContainer else colorScheme.onSurface,
        animationSpec = tween(TILE_COLOR_ANIMATION_MS),
        label = "TileLabelColor",
    )
    val chipColor by animateColorAsState(
        targetValue = if (selected) colorScheme.primary else colorScheme.surface,
        animationSpec = tween(TILE_COLOR_ANIMATION_MS),
        label = "TileChipColor",
    )
    val chipContentColor by animateColorAsState(
        targetValue = if (selected) colorScheme.onPrimary else colorScheme.onSurfaceVariant,
        animationSpec = tween(TILE_COLOR_ANIMATION_MS),
        label = "TileChipContentColor",
    )

    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(TileShape)
                .background(containerColor)
                .border(width = 1.dp, color = borderColor, shape = TileShape)
                .selectable(
                    selected = selected,
                    enabled = enabled,
                    role = Role.RadioButton,
                    onClick = onClick,
                )
                .padding(TilePadding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(TileIconChipSize)
                    .clip(KeepOnChipShape)
                    .background(chipColor),
                contentAlignment = Alignment.Center,
            ) {
                TilePreviewIcon(
                    option = option,
                    selected = selected,
                    enabled = enabled,
                    durationStep = durationStep,
                    replayTick = replayTick,
                    currentScreenTimeout = currentScreenTimeout,
                    timeoutIconStyle = timeoutIconStyle,
                    tint = chipContentColor,
                )
            }
            Spacer(modifier = Modifier.height(TileLabelSpacing))
            Text(
                text = option.label,
                style = MaterialTheme.typography.bodyMedium,
                color = labelColor,
                textAlign = TextAlign.Center,
                minLines = 2,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        AnimatedVisibility(
            visible = selected,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(TileBadgePadding),
        ) {
            Box(
                modifier = Modifier
                    .size(TileBadgeSize)
                    .clip(CircleShape)
                    .background(colorScheme.primary),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = colorScheme.onPrimary,
                    modifier = Modifier.size(TileBadgeIconSize),
                )
            }
        }
    }
}

/**
 * The tile's chip content: the transition glyph, replaced by the preview frames while one plays.
 * The preview replays when the tile becomes selected, when the selected tile is re-tapped
 * ([replayTick]), when the duration changes and when the feature is enabled — captured by
 * `previewKey`, with a short debounce coalescing the rapid changes of dragging the duration slider.
 * The icon style and the current timeout are effect keys but deliberately NOT part of `previewKey`:
 * changing one mid-preview cancels the in-flight play (so it never finishes with stale glyphs)
 * without replaying it.
 */
@Composable
private fun TilePreviewIcon(
    option: IconTransitionOptionUI,
    selected: Boolean,
    enabled: Boolean,
    durationStep: Int,
    replayTick: Int,
    currentScreenTimeout: ScreenTimeout,
    timeoutIconStyle: TimeoutIconStyle,
    tint: Color,
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val glyph = remember(option.id) { IconTransitionGlyphCatalog.glyphFor(option.id) }
    val glyphPainter = rememberVectorPainter(glyph)
    val transition = remember(option.id) { IconTransitionCatalog.fromId(option.id) }
    val durationMs = remember(durationStep) { IconTransitionTiming.durationMs(durationStep) }
    val colorFilter = remember(tint) { ColorFilter.tint(tint) }

    var previewFrame by remember { mutableStateOf<Bitmap?>(null) }
    val previewKey = if (enabled && selected) "${option.id}@$durationStep#$replayTick" else null
    var lastPreviewKey by remember { mutableStateOf(previewKey) }
    LaunchedEffect(previewKey, timeoutIconStyle, currentScreenTimeout) {
        if (previewKey == null) {
            // Deselected or disabled: no preview, and clear the key so a later replay triggers.
            previewFrame = null
            lastPreviewKey = null
            return@LaunchedEffect
        }
        if (previewKey == lastPreviewKey) {
            // First composition, or a style/timeout change relaunched the effect: nothing new to
            // preview — just drop any in-flight preview frame so the chip reflects the fresh inputs.
            previewFrame = null
            return@LaunchedEffect
        }
        lastPreviewKey = previewKey
        previewFrame = null // show the crisp glyph while the debounce settles
        delay(PREVIEW_DEBOUNCE_MS.milliseconds)
        val timeoutIcon = loadIconBitmap(context, currentScreenTimeout, timeoutIconStyle) ?: return@LaunchedEffect
        val glyphBitmap = renderGlyphBitmap(glyphPainter, density, layoutDirection, timeoutIcon.width, timeoutIcon.height)
        // Double pass: the tile's glyph morphs into the current timeout icon and back, so the
        // effect previews a real transition in both directions and the tile settles on its glyph.
        playPreviewPass(transition, glyphBitmap, timeoutIcon, durationMs) { frame -> previewFrame = frame }
        delay(PREVIEW_DEBOUNCE_MS.milliseconds)
        playPreviewPass(transition, timeoutIcon, glyphBitmap, durationMs) { frame -> previewFrame = frame }
        previewFrame = null
    }

    val frame = previewFrame
    if (frame != null) {
        Image(
            bitmap = frame.asImageBitmap(),
            contentDescription = null,
            colorFilter = colorFilter,
            modifier = Modifier.size(TileIconSize),
        )
    } else {
        Icon(
            imageVector = glyph,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(TileIconSize),
        )
    }
}

/** Plays one preview pass of [transition] from [from] to [to], compositing frames off the main thread. */
private suspend fun playPreviewPass(
    transition: IconTransition,
    from: Bitmap,
    to: Bitmap,
    durationMs: Int,
    emit: (Bitmap) -> Unit,
) = TransitionPlayer.play(
    transition = transition,
    from = from,
    to = to,
    durationMs = durationMs,
    maxFrames = IconTransitionTiming.FRAME_COUNT,
    renderContext = Dispatchers.Default,
    emitFrame = emit,
)

/**
 * Rasterizes the tile's vector glyph at the exact pixel size of the timeout-icon bitmap, so the
 * transition renderer works on two same-sized layers. Drawn in a flat opaque color: the preview
 * frames are tinted at display time, where only the alpha channel matters.
 */
private fun renderGlyphBitmap(
    painter: Painter,
    density: Density,
    layoutDirection: LayoutDirection,
    widthPx: Int,
    heightPx: Int,
): Bitmap {
    val imageBitmap = ImageBitmap(widthPx, heightPx)
    CanvasDrawScope().draw(
        density = density,
        layoutDirection = layoutDirection,
        canvas = Canvas(imageBitmap),
        size = Size(widthPx.toFloat(), heightPx.toFloat()),
    ) {
        with(painter) {
            draw(size = size, colorFilter = ColorFilter.tint(Color.White))
        }
    }
    return imageBitmap.asAndroidBitmap()
}

private val previewOptions = listOf(
    IconTransitionOptionUI(id = "liquid_morph", label = "Liquid morph"),
    IconTransitionOptionUI(id = "particles", label = "Particles"),
    IconTransitionOptionUI(id = "warp", label = "Turbulent warp"),
    IconTransitionOptionUI(id = "vortex", label = "Vortex"),
    IconTransitionOptionUI(id = "flip", label = "Flip"),
    IconTransitionOptionUI(id = "swipe_down", label = "Reel"),
)

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun IconTransitionTypeGridWidePreview() {
    KeepOnTheme(dynamicColor = false) {
        IconTransitionTypeGrid(
            options = previewOptions,
            animation = IconTransitionAnimation(typeId = "vortex"),
            currentScreenTimeout = ScreenTimeout(30000),
            timeoutIconStyle = TimeoutIconStyle(),
            onSelect = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true, widthDp = 220)
@Composable
private fun IconTransitionTypeGridNarrowPreview() {
    KeepOnTheme(dynamicColor = false) {
        IconTransitionTypeGrid(
            options = previewOptions,
            animation = IconTransitionAnimation(typeId = "vortex"),
            currentScreenTimeout = ScreenTimeout(30000),
            timeoutIconStyle = TimeoutIconStyle(),
            onSelect = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
