package fr.twentynine.keepon.ui.util

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit

private const val GLOW_ALPHA_ANIMATION_DURATION_MS = 600
private const val MAX_GLOW_ALPHA_VALUE = 0.6f

@Composable
fun GlowingText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontWeight: FontWeight? = null,
    showGlow: Boolean = true,
    glowColor: Color,
    glowRadius: Dp,
    glowSpread: Dp,
) {
    Box(contentAlignment = Alignment.Center, modifier = modifier) {
        val animatedGlowAlpha by animateFloatAsState(
            targetValue = if (showGlow) MAX_GLOW_ALPHA_VALUE else 0f,
            animationSpec = tween(GLOW_ALPHA_ANIMATION_DURATION_MS),
            label = "TextGlowAnimation"
        )
        val glowShadow = remember(animatedGlowAlpha, glowColor, glowRadius) {
            Shadow(
                color = glowColor.copy(alpha = animatedGlowAlpha),
                offset = Offset.Zero,
                blurRadius = glowRadius.value
            )
        }
        val glowStyle = remember(style, glowShadow) {
            style.copy(
                color = Color.Transparent,
                shadow = glowShadow,
            )
        }

        if (animatedGlowAlpha > 0f) {
            Text(
                text = text,
                style = glowStyle,
                fontSize = fontSize,
                fontWeight = fontWeight,
                modifier = Modifier.padding(glowSpread),
            )
        }

        Text(
            text = text,
            style = style,
            fontSize = fontSize,
            fontWeight = fontWeight,
            modifier = Modifier.padding(glowSpread),
        )
    }
}
