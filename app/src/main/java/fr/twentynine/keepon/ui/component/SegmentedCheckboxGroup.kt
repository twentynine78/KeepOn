package fr.twentynine.keepon.ui.component

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.twentynine.keepon.ui.theme.KeepOnTheme

private val SegmentHeight = 40.dp

/** Pill ends: half the segment height. */
private val SegmentCornerRadius = SegmentHeight / 2

private val SegmentIconSize = 18.dp
private val SegmentIconSpacing = 8.dp
private val SegmentContentHorizontalPadding = 12.dp
private val SegmentBorderWidth = 1.dp
private const val SEGMENT_COLOR_ANIMATION_MS = 200

/** One entry of a [SegmentedCheckboxGroup]: an independently toggleable option. */
class SegmentedCheckboxOption(
    val label: String,
    val checked: Boolean,
    val onCheckedChange: (Boolean) -> Unit,
    /** Shown when unchecked; replaced by a checkmark when checked. */
    val glyph: ImageVector,
)

/**
 * A connected multi-select button group (Material segmented-button look): outlined pill-ended
 * segments sharing their inner borders, each toggling independently — checked segments fill with
 * the primary container color and swap their [SegmentedCheckboxOption.glyph] for a checkmark.
 *
 * Responsive: the labels are measured with the real text style, and when the widest one would not
 * fit in an equal share of the available width the group lays out as a vertical stack instead
 * (first segment rounded on top, last on bottom), so it stays clean on narrow screens, large font
 * scales and verbose locales.
 */
@Composable
fun SegmentedCheckboxGroup(
    options: List<SegmentedCheckboxOption>,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier) {
        val labelStyle = MaterialTheme.typography.bodyMedium
        val textMeasurer = rememberTextMeasurer()
        val density = LocalDensity.current
        val labels = options.map { it.label }
        val maxAvailableWidth = maxWidth

        val fitsAsRow = remember(labels, labelStyle, maxAvailableWidth, density) {
            with(density) {
                val fixedWidthPerSegment =
                    SegmentIconSize + SegmentIconSpacing + SegmentContentHorizontalPadding * 2
                // Adjacent segments overlap by one border width, freeing a little room.
                val totalWidth = maxAvailableWidth + SegmentBorderWidth * (options.size - 1)
                val segmentWidth = totalWidth / options.size
                labels.all { label ->
                    val labelWidth = textMeasurer.measure(text = label, style = labelStyle).size.width
                    labelWidth.toDp() + fixedWidthPerSegment <= segmentWidth
                }
            }
        }

        if (fitsAsRow) {
            Row(horizontalArrangement = Arrangement.spacedBy(-SegmentBorderWidth)) {
                options.forEachIndexed { index, option ->
                    Segment(
                        option = option,
                        shape = segmentShape(index, options.size, horizontal = true),
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(-SegmentBorderWidth)) {
                options.forEachIndexed { index, option ->
                    Segment(
                        option = option,
                        shape = segmentShape(index, options.size, horizontal = false),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

/** Rounds the group's outer corners only: the ends of the row, or the top/bottom of the column. */
private fun segmentShape(index: Int, count: Int, horizontal: Boolean): Shape {
    val isFirst = index == 0
    val isLast = index == count - 1
    return when {
        isFirst && isLast -> RoundedCornerShape(SegmentCornerRadius)
        horizontal && isFirst -> RoundedCornerShape(
            topStart = SegmentCornerRadius,
            bottomStart = SegmentCornerRadius,
        )
        horizontal && isLast -> RoundedCornerShape(
            topEnd = SegmentCornerRadius,
            bottomEnd = SegmentCornerRadius,
        )
        !horizontal && isFirst -> RoundedCornerShape(
            topStart = SegmentCornerRadius,
            topEnd = SegmentCornerRadius,
        )
        !horizontal && isLast -> RoundedCornerShape(
            bottomStart = SegmentCornerRadius,
            bottomEnd = SegmentCornerRadius,
        )
        else -> RoundedCornerShape(0.dp)
    }
}

@Composable
private fun Segment(
    option: SegmentedCheckboxOption,
    shape: Shape,
    modifier: Modifier = Modifier,
) {
    val containerColor by animateColorAsState(
        targetValue = if (option.checked) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            Color.Transparent
        },
        animationSpec = tween(SEGMENT_COLOR_ANIMATION_MS),
        label = "SegmentContainerColor",
    )
    val contentColor by animateColorAsState(
        targetValue = if (option.checked) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        animationSpec = tween(SEGMENT_COLOR_ANIMATION_MS),
        label = "SegmentContentColor",
    )

    Row(
        modifier = modifier
            .height(SegmentHeight)
            .clip(shape)
            .background(containerColor)
            .border(SegmentBorderWidth, MaterialTheme.colorScheme.outline, shape)
            .toggleable(
                value = option.checked,
                role = Role.Checkbox,
                onValueChange = option.onCheckedChange,
            )
            .padding(horizontal = SegmentContentHorizontalPadding),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Crossfade(targetState = option.checked, label = "SegmentIcon") { checked ->
            Icon(
                imageVector = if (checked) Icons.Filled.Check else option.glyph,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(SegmentIconSize),
            )
        }
        Spacer(modifier = Modifier.width(SegmentIconSpacing))
        Text(
            text = option.label,
            style = MaterialTheme.typography.bodyMedium,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun previewOptions() = listOf(
    SegmentedCheckboxOption("Bold", checked = false, onCheckedChange = {}, glyph = Icons.Filled.FormatBold),
    SegmentedCheckboxOption("Italic", checked = true, onCheckedChange = {}, glyph = Icons.Filled.FormatItalic),
    SegmentedCheckboxOption("Underline", checked = true, onCheckedChange = {}, glyph = Icons.Filled.FormatUnderlined),
)

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun SegmentedCheckboxGroupRowPreview() {
    KeepOnTheme(dynamicColor = false) {
        SegmentedCheckboxGroup(
            options = previewOptions(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        )
    }
}

@Preview(showBackground = true, widthDp = 200)
@Composable
private fun SegmentedCheckboxGroupColumnPreview() {
    KeepOnTheme(dynamicColor = false) {
        SegmentedCheckboxGroup(
            options = previewOptions(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        )
    }
}
