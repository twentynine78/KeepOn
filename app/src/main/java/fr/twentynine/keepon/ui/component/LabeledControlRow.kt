package fr.twentynine.keepon.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import fr.twentynine.keepon.ui.theme.StyleContentInset
import fr.twentynine.keepon.ui.theme.StyleControlLabelSpacing
import fr.twentynine.keepon.ui.theme.StyleControlRowVerticalPadding
import fr.twentynine.keepon.ui.theme.StyleControlSlotWidth

/**
 * A full-width, clickable settings row: a fixed-width leading slot followed by a label, so labels
 * share one column. The control is left-aligned in the slot and shifted left by [leadingGlyphInset]
 * so its glyph lands on the shared content rail — pass `StyleRadioGlyphInset` for radios/checkboxes
 * (Material insets them in a 48dp touch target) and 0 for switches. The control should be passive
 * (onClick / onCheckedChange = null) — the whole row handles the click.
 */
@Composable
fun LabeledControlRow(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    verticalPadding: Dp = StyleControlRowVerticalPadding,
    leadingGlyphInset: Dp = 0.dp,
    leading: @Composable () -> Unit,
    label: @Composable () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = StyleContentInset, vertical = verticalPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.width(StyleControlSlotWidth),
            contentAlignment = Alignment.CenterStart,
        ) {
            Box(modifier = Modifier.offset(x = -leadingGlyphInset)) {
                leading()
            }
        }
        Box(modifier = Modifier.weight(1f).padding(start = StyleControlLabelSpacing)) {
            label()
        }
    }
}
