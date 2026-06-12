package fr.twentynine.keepon.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.selection.triStateToggleable
import androidx.compose.material3.CheckboxColors
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ripple
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import kotlin.math.floor
import kotlin.math.max

/** Corner radius giving the box the same rounded personality as the app's cards and chips. */
private val RoundedCheckboxCornerRadius = 6.dp

private const val SNAP_ANIMATION_DELAY = 100
private val CheckboxDefaultPadding = 0.dp
private val CheckboxSize = 22.dp
private val CheckboxRippleRadius = 20.dp

// The standard MotionScheme springs the stock checkbox resolves to (MaterialTheme.motionScheme is
// still internal in material3 1.4.0, so the StandardMotionTokens values are inlined here):
// DefaultSpatial drives the checkmark draw, DefaultEffects the box-in colors, FastEffects box-out.
private fun <T> defaultSpatialSpec() = spring<T>(dampingRatio = 0.9f, stiffness = 700f)
private fun <T> defaultEffectsSpec() = spring<T>(dampingRatio = 1f, stiffness = 1600f)
private fun <T> fastEffectsSpec() = spring<T>(dampingRatio = 1f, stiffness = 3800f)

/**
 * The Material 3 [androidx.compose.material3.Checkbox] with a configurable [cornerRadius] (M3 hard
 * codes a 2dp radius with no shape parameter). Adapted from the material3 1.4.0 source (Apache 2.0):
 * everything else — the progressive checkmark draw, the motion-scheme animation specs, the per-state
 * colors and their transitions, the unbounded ripple and the touch-target sizing — is kept identical
 * so it behaves exactly like the stock checkbox.
 */
@Composable
fun RoundedCheckbox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    cornerRadius: Dp = RoundedCheckboxCornerRadius,
    colors: CheckboxColors = CheckboxDefaults.colors(),
    interactionSource: MutableInteractionSource? = null,
) {
    val strokeWidthPx = with(LocalDensity.current) { floor(CheckboxDefaults.StrokeWidth.toPx()) }
    val state = ToggleableState(checked)
    val toggleableModifier =
        if (onCheckedChange != null) {
            Modifier.triStateToggleable(
                state = state,
                onClick = { onCheckedChange(!checked) },
                enabled = enabled,
                role = Role.Checkbox,
                interactionSource = interactionSource,
                indication = ripple(bounded = false, radius = CheckboxRippleRadius),
            )
        } else {
            Modifier
        }
    CheckboxImpl(
        enabled = enabled,
        value = state,
        modifier =
        modifier
            .then(
                if (onCheckedChange != null) {
                    Modifier.minimumInteractiveComponentSize()
                } else {
                    Modifier
                }
            )
            .then(toggleableModifier)
            .padding(CheckboxDefaultPadding),
        colors = colors,
        cornerRadius = cornerRadius,
        checkmarkStroke = Stroke(width = strokeWidthPx, cap = StrokeCap.Square),
        outlineStroke = Stroke(width = strokeWidthPx),
    )
}

@Composable
private fun CheckboxImpl(
    enabled: Boolean,
    value: ToggleableState,
    modifier: Modifier,
    colors: CheckboxColors,
    cornerRadius: Dp,
    checkmarkStroke: Stroke,
    outlineStroke: Stroke,
) {
    val transition = updateTransition(value)
    val defaultAnimationSpec = defaultSpatialSpec<Float>()
    val checkDrawFraction =
        transition.animateFloat(
            transitionSpec = {
                when {
                    initialState == ToggleableState.Off -> defaultAnimationSpec
                    targetState == ToggleableState.Off -> snap(delayMillis = SNAP_ANIMATION_DELAY)
                    else -> defaultAnimationSpec
                }
            }
        ) {
            when (it) {
                ToggleableState.On -> 1f
                ToggleableState.Off -> 0f
                ToggleableState.Indeterminate -> 1f
            }
        }

    val checkCenterGravitationShiftFraction =
        transition.animateFloat(
            transitionSpec = {
                when {
                    initialState == ToggleableState.Off -> snap()
                    targetState == ToggleableState.Off -> snap(delayMillis = SNAP_ANIMATION_DELAY)
                    else -> defaultAnimationSpec
                }
            }
        ) {
            when (it) {
                ToggleableState.On -> 0f
                ToggleableState.Off -> 0f
                ToggleableState.Indeterminate -> 1f
            }
        }
    val checkCache = remember { CheckDrawingCache() }
    val checkColor = colors.animatedCheckmarkColor(value)
    val boxColor = colors.animatedBoxColor(enabled, value)
    val borderColor = colors.animatedBorderColor(enabled, value)
    Canvas(modifier.wrapContentSize(Alignment.Center).requiredSize(CheckboxSize)) {
        drawBox(
            boxColor = boxColor.value,
            borderColor = borderColor.value,
            radius = cornerRadius.toPx(),
            stroke = outlineStroke,
        )
        drawCheck(
            checkColor = checkColor.value,
            checkFraction = checkDrawFraction.value,
            crossCenterGravitation = checkCenterGravitationShiftFraction.value,
            stroke = checkmarkStroke,
            drawingCache = checkCache,
        )
    }
}

private fun DrawScope.drawBox(boxColor: Color, borderColor: Color, radius: Float, stroke: Stroke) {
    val halfStrokeWidth = stroke.width / 2.0f
    val checkboxSize = size.width
    if (boxColor == borderColor) {
        drawRoundRect(
            boxColor,
            size = Size(checkboxSize, checkboxSize),
            cornerRadius = CornerRadius(radius),
            style = Fill,
        )
    } else {
        drawRoundRect(
            boxColor,
            topLeft = Offset(stroke.width, stroke.width),
            size = Size(checkboxSize - stroke.width * 2, checkboxSize - stroke.width * 2),
            cornerRadius = CornerRadius(max(0f, radius - stroke.width)),
            style = Fill,
        )
        drawRoundRect(
            borderColor,
            topLeft = Offset(halfStrokeWidth, halfStrokeWidth),
            size = Size(checkboxSize - stroke.width, checkboxSize - stroke.width),
            cornerRadius = CornerRadius(radius - halfStrokeWidth),
            style = stroke,
        )
    }
}

private fun DrawScope.drawCheck(
    checkColor: Color,
    checkFraction: Float,
    crossCenterGravitation: Float,
    stroke: Stroke,
    drawingCache: CheckDrawingCache,
) {
    val width = size.width
    val checkCrossX = 0.4f
    val checkCrossY = 0.7f
    val leftX = 0.2f
    val leftY = 0.5f
    val rightX = 0.8f
    val rightY = 0.3f

    val gravitatedCrossX = lerp(checkCrossX, 0.5f, crossCenterGravitation)
    val gravitatedCrossY = lerp(checkCrossY, 0.5f, crossCenterGravitation)
    // gravitate only Y for end to achieve center line
    val gravitatedLeftY = lerp(leftY, 0.5f, crossCenterGravitation)
    val gravitatedRightY = lerp(rightY, 0.5f, crossCenterGravitation)

    with(drawingCache) {
        checkPath.rewind()
        checkPath.moveTo(width * leftX, width * gravitatedLeftY)
        checkPath.lineTo(width * gravitatedCrossX, width * gravitatedCrossY)
        checkPath.lineTo(width * rightX, width * gravitatedRightY)
        pathMeasure.setPath(checkPath, false)
        pathToDraw.rewind()
        pathMeasure.getSegment(0f, pathMeasure.length * checkFraction, pathToDraw, true)
    }
    drawPath(drawingCache.pathToDraw, checkColor, style = stroke)
}

@Immutable
private class CheckDrawingCache(
    val checkPath: Path = Path(),
    val pathMeasure: PathMeasure = PathMeasure(),
    val pathToDraw: Path = Path(),
)

/** Mirrors the internal `CheckboxColors.checkmarkColor` resolution of material3. */
@Composable
private fun CheckboxColors.animatedCheckmarkColor(state: ToggleableState): State<Color> {
    val target =
        if (state == ToggleableState.Off) {
            uncheckedCheckmarkColor
        } else {
            checkedCheckmarkColor
        }

    return animateColorAsState(target, colorAnimationSpecForState(state))
}

/** Mirrors the internal `CheckboxColors.boxColor` resolution of material3. */
@Composable
private fun CheckboxColors.animatedBoxColor(enabled: Boolean, state: ToggleableState): State<Color> {
    val target =
        if (enabled) {
            when (state) {
                ToggleableState.On,
                ToggleableState.Indeterminate -> checkedBoxColor
                ToggleableState.Off -> uncheckedBoxColor
            }
        } else {
            when (state) {
                ToggleableState.On -> disabledCheckedBoxColor
                ToggleableState.Indeterminate -> disabledIndeterminateBoxColor
                ToggleableState.Off -> disabledUncheckedBoxColor
            }
        }

    // If not enabled 'snap' to the disabled state, as there should be no animations between
    // enabled / disabled.
    return if (enabled) {
        animateColorAsState(target, colorAnimationSpecForState(state))
    } else {
        rememberUpdatedState(target)
    }
}

/** Mirrors the internal `CheckboxColors.borderColor` resolution of material3. */
@Composable
private fun CheckboxColors.animatedBorderColor(enabled: Boolean, state: ToggleableState): State<Color> {
    val target =
        if (enabled) {
            when (state) {
                ToggleableState.On,
                ToggleableState.Indeterminate -> checkedBorderColor
                ToggleableState.Off -> uncheckedBorderColor
            }
        } else {
            when (state) {
                ToggleableState.Indeterminate -> disabledIndeterminateBorderColor
                ToggleableState.On -> disabledBorderColor
                ToggleableState.Off -> disabledUncheckedBorderColor
            }
        }

    // If not enabled 'snap' to the disabled state, as there should be no animations between
    // enabled / disabled.
    return if (enabled) {
        animateColorAsState(target, colorAnimationSpecForState(state))
    } else {
        rememberUpdatedState(target)
    }
}

/** Returns the color [AnimationSpec] for the given state (box out fast, box in default). */
private fun colorAnimationSpecForState(state: ToggleableState): AnimationSpec<Color> {
    return if (state == ToggleableState.Off) {
        fastEffectsSpec()
    } else {
        defaultEffectsSpec()
    }
}
