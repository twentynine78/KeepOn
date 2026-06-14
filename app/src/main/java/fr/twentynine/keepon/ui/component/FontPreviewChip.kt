package fr.twentynine.keepon.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.twentynine.keepon.R
import fr.twentynine.keepon.ui.theme.CHIP_BACKGROUND_ALPHA
import fr.twentynine.keepon.ui.theme.CHIP_BORDER_ALPHA
import fr.twentynine.keepon.ui.theme.KeepOnChipShape
import fr.twentynine.keepon.ui.theme.KeepOnChipSize
import fr.twentynine.keepon.ui.theme.KeepOnTheme

private val GlyphFontSize = 16.sp
private const val CHIP_COLOR_ANIMATION_MS = 400

/**
 * The rounded chip that previews a selectable font as an "Aa" glyph drawn in that font, used by the
 * Style screen's font list rows. When [selected] it fills with the primary color (border included,
 * so it reads as borderless), mirroring the selected chip of the transition-type tiles.
 */
@Composable
fun FontPreviewChip(
    fontFamily: FontFamily,
    selected: Boolean,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val backgroundColor by animateColorAsState(
        targetValue = if (selected) {
            colorScheme.primary
        } else {
            colorScheme.background.copy(alpha = CHIP_BACKGROUND_ALPHA)
        },
        animationSpec = tween(CHIP_COLOR_ANIMATION_MS),
        label = "FontChipBackgroundColor",
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) {
            colorScheme.primary
        } else {
            colorScheme.outline.copy(alpha = CHIP_BORDER_ALPHA)
        },
        animationSpec = tween(CHIP_COLOR_ANIMATION_MS),
        label = "FontChipBorderColor",
    )
    val glyphColor by animateColorAsState(
        targetValue = if (selected) colorScheme.onPrimary else colorScheme.onSurfaceVariant,
        animationSpec = tween(CHIP_COLOR_ANIMATION_MS),
        label = "FontChipGlyphColor",
    )

    Box(
        modifier = modifier
            .clip(KeepOnChipShape)
            .background(backgroundColor)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = KeepOnChipShape,
            )
            .size(KeepOnChipSize),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.font_preview_glyph),
            fontFamily = fontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = GlyphFontSize,
            color = glyphColor,
            maxLines = 1,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FontPreviewChipPreview() {
    KeepOnTheme(dynamicColor = false) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FontPreviewChip(fontFamily = FontFamily.Default, selected = false)
            FontPreviewChip(fontFamily = FontFamily.Serif, selected = true)
        }
    }
}
