package fr.twentynine.keepon.ui.util

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import kotlin.math.hypot

/**
 * Draws a [width]-thick rounded-rect border whose color is a sweep (conic) gradient over [colors]
 * that appears to rotate continuously around the element, while the rounded outline itself stays
 * static (the corners never spin).
 *
 * The rotation [angle] (in degrees) is read through a provider lambda inside the draw phase only,
 * so an infinite transition driving it invalidates drawing every frame WITHOUT recomposing or
 * relaying out. Create the transition in the calling composable and pass `{ state.value }`.
 *
 * [colors] should start and end with the same color (e.g. listOf(primary, secondary, primary)) so
 * the sweep gradient has no visible seam at its 360 -> 0 wrap.
 *
 * The gradient is clipped to a ring band (outer rounded rect minus the inner one inset by [width]),
 * so only the border is painted and the element's own content stays untouched.
 *
 * @param cornerRadius corner radius of the rounded rect (match the element's own shape).
 */
fun Modifier.rotatingGradientBorder(
    angle: () -> Float,
    colors: List<Color>,
    width: Dp,
    cornerRadius: Dp,
): Modifier = this.drawWithCache {
    // Cache block: depends only on size / colors / width / cornerRadius — never on the angle.
    val strokePx = width.toPx()
    val outerRadiusPx = cornerRadius.toPx()
    val center = Offset(size.width / 2f, size.height / 2f)

    // Ring band = outer rounded rect minus the inner one inset by the stroke width (concentric
    // corners keep a constant-width border with no corner artefacts).
    val outerPath = Path().apply {
        addRoundRect(RoundRect(Rect(Offset.Zero, size), CornerRadius(outerRadiusPx)))
    }
    val innerPath = Path().apply {
        addRoundRect(
            RoundRect(
                Rect(Offset(strokePx, strokePx), Size(size.width - strokePx * 2f, size.height - strokePx * 2f)),
                CornerRadius((outerRadiusPx - strokePx).coerceAtLeast(0f)),
            )
        )
    }
    val ringPath = Path().apply { op(outerPath, innerPath, PathOperation.Difference) }

    val brush = Brush.sweepGradient(colors = colors, center = center)

    // Oversize the gradient rect to the diagonal so it still covers the ring at any rotation
    // (worst case 45 deg). The clip keeps only the ring band, so the overhang costs nothing.
    val diagonal = hypot(size.width, size.height)
    val coverTopLeft = Offset(center.x - diagonal / 2f, center.y - diagonal / 2f)
    val coverSize = Size(diagonal, diagonal)

    onDrawWithContent {
        drawContent()
        clipPath(ringPath) {
            rotate(degrees = angle(), pivot = center) {
                drawRect(brush = brush, topLeft = coverTopLeft, size = coverSize)
            }
        }
    }
}
