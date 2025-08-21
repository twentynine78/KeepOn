package fr.twentynine.keepon.ui.view

import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import fr.twentynine.keepon.data.enums.ItemPosition
import fr.twentynine.keepon.data.model.ScreenTimeoutUI

private const val SCREEN_TIMEOUT_CARD_SWIPE_THRESHOLD = 0.30f

@Composable
fun SwipeableScreenTimeoutUICardView(
    item: ScreenTimeoutUI,
    itemPosition: ItemPosition,
    modifier: Modifier = Modifier,
    swipeEnabled: Boolean = true,
    onClickAction: ((ScreenTimeoutUI) -> Unit)?,
    onSwipeAction: (SwipeToDismissBoxValue, ScreenTimeoutUI) -> Unit,
    content: @Composable (ScreenTimeoutUI) -> Unit
) {
    SwipeableItemCardView(
        item = item,
        itemPosition = itemPosition,
        modifier = modifier,
        swipeEnabled = swipeEnabled,
        onClickAction = onClickAction,
        onSwipeAction = onSwipeAction,
        swipeThresholdFraction = SCREEN_TIMEOUT_CARD_SWIPE_THRESHOLD,
        backgroundContent = { dismissDirection, dismissProgress, currentItem ->
            ScreenTimeoutSetDefaultDismissActionRowView(
                dismissDirection = dismissDirection,
                dismissProgress = dismissProgress,
                screenTimeoutUI = currentItem,
                swipeEnabledState = swipeEnabled,
                swipeThresholdFraction = SCREEN_TIMEOUT_CARD_SWIPE_THRESHOLD,
            )
        },
        content = content
    )
}
