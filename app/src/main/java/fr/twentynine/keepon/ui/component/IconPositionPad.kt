package fr.twentynine.keepon.ui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import fr.twentynine.keepon.R
import fr.twentynine.keepon.ui.theme.KeepOnTheme
import kotlin.math.roundToInt

private val PadShape = RoundedCornerShape(16.dp)
private const val PAD_ASPECT_RATIO = 1.5f
private const val PAD_BACKGROUND_ALPHA = 0.6f
private const val PAD_BORDER_ALPHA = 0.35f
private const val CENTER_GUIDE_ALPHA = 0.6f
private const val KNOB_CROSSHAIR_ALPHA = 0.35f

/** Inset of the knob-center travel area inside the pad, so the knob never clips the edges. */
private val PadContentInset = 20.dp

private val KnobSize = 28.dp
private val KnobInnerSize = 20.dp
private val PadLegendPadding = 10.dp
private val ReadoutTopPadding = 8.dp
private val ReadoutValueSpacing = 4.dp
private val ReadoutGroupSpacing = 16.dp

/**
 * A 2D position picker replacing a pair of horizontal/vertical sliders: a rounded pad where a
 * draggable knob sets both axes at once, snapping to the integer notches of [range] (center =
 * 0,0 default). WYSIWYG mapping: dragging right/up increases [horizontal]/[vertical], matching the
 * directions the icon generator moves the glyph. Center guides plus a crosshair through the knob
 * mirror the current offset; readouts (and a drag hint) sit below the pad. Tapping places the knob
 * directly; releasing animates it onto the nearest notch.
 */
@Composable
fun IconPositionPad(
    horizontal: Int,
    vertical: Int,
    onPositionChange: (horizontal: Int, vertical: Int) -> Unit,
    modifier: Modifier = Modifier,
    range: IntRange = -5..5,
) {
    val colorScheme = MaterialTheme.colorScheme
    val insetPx = with(LocalDensity.current) { PadContentInset.toPx() }
    val guideStrokePx = with(LocalDensity.current) { 1.dp.toPx() }

    var padSize by remember { mutableStateOf(IntSize.Zero) }
    // The finger position while dragging (clamped to the travel area), null when settled.
    var dragPosition by remember { mutableStateOf<Offset?>(null) }

    val currentHorizontal by rememberUpdatedState(horizontal)
    val currentVertical by rememberUpdatedState(vertical)
    val currentOnPositionChange by rememberUpdatedState(onPositionChange)

    // The knob follows the finger instantly while dragging, then springs onto the snapped notch.
    val knobPosition = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    var knobInitialised by remember { mutableStateOf(false) }
    val knobTarget = if (padSize == IntSize.Zero) {
        null
    } else {
        dragPosition ?: valueToPosition(horizontal, vertical, padSize, insetPx, range)
    }
    LaunchedEffect(knobTarget) {
        val target = knobTarget ?: return@LaunchedEffect
        when {
            !knobInitialised -> {
                knobPosition.snapTo(target)
                knobInitialised = true
            }
            dragPosition != null -> knobPosition.snapTo(target)
            else -> knobPosition.animateTo(target, spring(stiffness = Spring.StiffnessMedium))
        }
    }

    val horizontalLabel = stringResource(R.string.font_options_position_horizontal)
    val verticalLabel = stringResource(R.string.font_options_position_vertical)

    fun emitPosition(position: Offset) {
        val size = padSize
        if (size == IntSize.Zero) return
        val (newHorizontal, newVertical) = positionToValues(position, size, insetPx, range)
        if (newHorizontal != currentHorizontal || newVertical != currentVertical) {
            currentOnPositionChange(newHorizontal, newVertical)
        }
    }

    fun clampToTravelArea(position: Offset): Offset {
        val size = padSize
        if (size == IntSize.Zero) return position
        return Offset(
            x = position.x.coerceIn(insetPx, size.width - insetPx),
            y = position.y.coerceIn(insetPx, size.height - insetPx),
        )
    }

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(PAD_ASPECT_RATIO)
                .clip(PadShape)
                .background(colorScheme.surfaceVariant.copy(alpha = PAD_BACKGROUND_ALPHA))
                .border(
                    width = 1.dp,
                    color = colorScheme.outline.copy(alpha = PAD_BORDER_ALPHA),
                    shape = PadShape,
                )
                .onSizeChanged { padSize = it }
                .pointerInput(range) {
                    detectDragGestures(
                        onDragStart = { startPosition ->
                            val clamped = clampToTravelArea(startPosition)
                            dragPosition = clamped
                            emitPosition(clamped)
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            val clamped = clampToTravelArea(change.position)
                            dragPosition = clamped
                            emitPosition(clamped)
                        },
                        onDragEnd = { dragPosition = null },
                        onDragCancel = { dragPosition = null },
                    )
                }
                .pointerInput(range) {
                    detectTapGestures { tapPosition ->
                        emitPosition(clampToTravelArea(tapPosition))
                    }
                }
                .semantics {
                    stateDescription =
                        "$horizontalLabel ${signedLabel(horizontal)}, $verticalLabel ${signedLabel(vertical)}"
                    customActions = listOf(
                        CustomAccessibilityAction("$horizontalLabel +1") {
                            currentOnPositionChange((horizontal + 1).coerceAtMost(range.last), vertical)
                            true
                        },
                        CustomAccessibilityAction("$horizontalLabel -1") {
                            currentOnPositionChange((horizontal - 1).coerceAtLeast(range.first), vertical)
                            true
                        },
                        CustomAccessibilityAction("$verticalLabel +1") {
                            currentOnPositionChange(horizontal, (vertical + 1).coerceAtMost(range.last))
                            true
                        },
                        CustomAccessibilityAction("$verticalLabel -1") {
                            currentOnPositionChange(horizontal, (vertical - 1).coerceAtLeast(range.first))
                            true
                        },
                    )
                }
                .drawBehind {
                    if (padSize == IntSize.Zero) return@drawBehind
                    // Static guides through the pad center (the 0,0 notch).
                    val guideColor = colorScheme.outlineVariant.copy(alpha = CENTER_GUIDE_ALPHA)
                    drawLine(
                        color = guideColor,
                        start = Offset(size.width / 2f, 0f),
                        end = Offset(size.width / 2f, size.height),
                        strokeWidth = guideStrokePx,
                    )
                    drawLine(
                        color = guideColor,
                        start = Offset(0f, size.height / 2f),
                        end = Offset(size.width, size.height / 2f),
                        strokeWidth = guideStrokePx,
                    )
                    // Crosshair through the knob (coincides with the guides at the center).
                    val crosshairColor = colorScheme.primary.copy(alpha = KNOB_CROSSHAIR_ALPHA)
                    val knob = knobPosition.value
                    drawLine(
                        color = crosshairColor,
                        start = Offset(knob.x, 0f),
                        end = Offset(knob.x, size.height),
                        strokeWidth = guideStrokePx,
                    )
                    drawLine(
                        color = crosshairColor,
                        start = Offset(0f, knob.y),
                        end = Offset(size.width, knob.y),
                        strokeWidth = guideStrokePx,
                    )
                },
        ) {
            if (padSize != IntSize.Zero) {
                val knobRadiusPx = with(LocalDensity.current) { (KnobSize / 2).toPx() }
                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                x = (knobPosition.value.x - knobRadiusPx).roundToInt(),
                                y = (knobPosition.value.y - knobRadiusPx).roundToInt(),
                            )
                        }
                        .size(KnobSize)
                        .shadow(elevation = 2.dp, shape = CircleShape)
                        .background(colorScheme.surface, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(KnobInnerSize)
                            .clip(CircleShape)
                            .background(colorScheme.primary),
                    )
                }
            }
            Text(
                text = "H · V",
                style = MaterialTheme.typography.labelMedium,
                color = colorScheme.outline,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(PadLegendPadding),
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = ReadoutTopPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = horizontalLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.width(ReadoutValueSpacing))
            Text(
                text = signedLabel(horizontal),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.width(ReadoutGroupSpacing))
            Text(
                text = verticalLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.width(ReadoutValueSpacing))
            Text(
                text = signedLabel(vertical),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = stringResource(R.string.font_options_position_drag_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.outline,
            )
        }
    }
}

/** Knob-center position of the ([horizontal], [vertical]) notch; up = positive vertical (WYSIWYG). */
private fun valueToPosition(
    horizontal: Int,
    vertical: Int,
    size: IntSize,
    insetPx: Float,
    range: IntRange,
): Offset {
    val span = (range.last - range.first).toFloat()
    val fractionX = (horizontal - range.first) / span
    val fractionY = (range.last - vertical) / span
    return Offset(
        x = insetPx + fractionX * (size.width - 2 * insetPx),
        y = insetPx + fractionY * (size.height - 2 * insetPx),
    )
}

/** Nearest ([range] x [range]) notch for a pad [position]; up = positive vertical (WYSIWYG). */
private fun positionToValues(
    position: Offset,
    size: IntSize,
    insetPx: Float,
    range: IntRange,
): Pair<Int, Int> {
    val span = range.last - range.first
    val fractionX = ((position.x - insetPx) / (size.width - 2 * insetPx)).coerceIn(0f, 1f)
    val fractionY = ((position.y - insetPx) / (size.height - 2 * insetPx)).coerceIn(0f, 1f)
    val horizontal = (range.first + fractionX * span).roundToInt()
    val vertical = (range.last - fractionY * span).roundToInt()
    return horizontal to vertical
}

/** Formats a notch value with an explicit sign, matching the readouts ("+2", "-2", "0"). */
private fun signedLabel(value: Int): String = if (value > 0) "+$value" else value.toString()

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun IconPositionPadCenteredPreview() {
    KeepOnTheme(dynamicColor = false) {
        IconPositionPad(
            horizontal = 0,
            vertical = 0,
            onPositionChange = { _, _ -> },
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun IconPositionPadOffsetPreview() {
    KeepOnTheme(dynamicColor = false) {
        IconPositionPad(
            horizontal = 3,
            vertical = -2,
            onPositionChange = { _, _ -> },
            modifier = Modifier.padding(16.dp),
        )
    }
}
