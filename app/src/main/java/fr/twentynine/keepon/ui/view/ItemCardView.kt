package fr.twentynine.keepon.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import fr.twentynine.keepon.data.enums.ItemPosition

@Composable
fun ItemCardView(
    itemPosition: ItemPosition,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    SwipeableItemCardView(
        item = null,
        itemPosition = itemPosition,
        modifier = modifier,
        swipeEnabled = false,
        onClickAction = { if (onClick != null) onClick() },
        onSwipeAction = null,
        backgroundContent = null,
        content = { content() }
    )
}
