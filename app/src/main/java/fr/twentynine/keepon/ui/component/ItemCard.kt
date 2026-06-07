package fr.twentynine.keepon.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import fr.twentynine.keepon.ui.model.ItemPosition

/**
 * A static (non-swipeable) list card. Thin wrapper over [PositionedCard]; clicks, if any, are
 * handled by the [content] itself.
 */
@Composable
fun ItemCard(
    itemPosition: ItemPosition,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    PositionedCard(itemPosition = itemPosition, modifier = modifier) {
        content()
    }
}
