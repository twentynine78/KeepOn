package fr.twentynine.keepon.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.clearAndSetSemantics

/**
 * A [Text] that is at least as tall as every one of [ghostTexts], by stacking them underneath it,
 * invisible (alpha 0) and ignored by accessibility services. Used for sibling rows shown on
 * different screens: each row lists the other's label as a ghost, so both rows resolve to the same
 * height — whatever the locale, font scale or screen width — and the controls centered against them
 * line up across screens. The visible text stays vertically centered in the reserved block.
 */
@Composable
fun GhostSizedText(
    text: String,
    ghostTexts: List<String>,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.CenterStart,
    ) {
        ghostTexts.forEach { ghost ->
            Text(
                text = ghost,
                modifier = Modifier
                    .alpha(0f)
                    .clearAndSetSemantics {},
            )
        }
        Text(text = text)
    }
}
