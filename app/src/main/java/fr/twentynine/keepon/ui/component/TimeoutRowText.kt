package fr.twentynine.keepon.ui.component

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import fr.twentynine.keepon.ui.theme.DefaultBadgeMinWidth
import fr.twentynine.keepon.ui.theme.LabelBadgeGap

/**
 * Lays out a timeout row's label and its optional "default" pill ([badge]) inside the bounded width
 * given by [modifier] (the caller sizes the slot to stop just before the trailing checkbox/lock).
 *
 * The label keeps priority so the timeout name stays readable: while the full label plus a minimal
 * pill fit, the pill stays (shrinking, its own text ellipsizing, down to [DefaultBadgeMinWidth]);
 * once even that no longer fits the pill is dropped and the label keeps its full width; only when the
 * label alone overflows does it finally ellipsize. At normal widths everything fits, so this renders
 * identically to a plain row (the pill's cross-fade animation lives in the [badge] slot).
 *
 * @param badge slot for the "default" pill, invoked with whether it should be shown and the max width
 *  it may occupy ([Dp.Unspecified] = unconstrained, e.g. while it is fading out).
 */
@Composable
fun TimeoutRowText(
    label: String,
    badgeVisible: Boolean,
    modifier: Modifier = Modifier,
    badge: @Composable (visible: Boolean, maxWidth: Dp) -> Unit,
) {
    BoxWithConstraints(modifier = modifier) {
        val density = LocalDensity.current
        val measurer = rememberTextMeasurer()
        // Must mirror TimeoutRowLabel's style exactly so the measured width matches what is drawn.
        val titleSmall = MaterialTheme.typography.titleSmall
        val labelStyle = remember(titleSmall) {
            titleSmall.copy(fontSize = 17.sp, fontWeight = FontWeight.Bold)
        }

        // True single-line width of the label (softWrap = false → the whole line, not the widest word).
        val labelFull = remember(label, labelStyle, density) {
            with(density) {
                measurer.measure(label, labelStyle, softWrap = false, maxLines = 1).size.width.toDp()
            }
        }

        val maxW = maxWidth
        val showBadge = badgeVisible && labelFull + LabelBadgeGap + DefaultBadgeMinWidth <= maxW
        val badgeMaxWidth = if (showBadge) {
            (maxW - labelFull - LabelBadgeGap).coerceAtLeast(DefaultBadgeMinWidth)
        } else {
            Dp.Unspecified
        }
        val labelWidth = if (showBadge) labelFull else labelFull.coerceAtMost(maxW)

        Row(verticalAlignment = Alignment.CenterVertically) {
            TimeoutRowLabel(
                text = label,
                modifier = Modifier.width(labelWidth),
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
            )
            badge(showBadge, badgeMaxWidth)
        }
    }
}
