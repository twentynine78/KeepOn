package fr.twentynine.keepon.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import fr.twentynine.keepon.ui.model.ItemPosition
import fr.twentynine.keepon.ui.model.ScreenTimeoutUI

private const val SCREEN_TIMEOUT_CARD_SWIPE_THRESHOLD = 0.30f

/** Duration of the selected-row tint fade, in step with the Home row markers (dot, badge). */
private const val SELECTED_ROW_TINT_ANIMATION_MS = 450

/**
 * A timeout row specialized on [SwipeableItemCard]: tap to toggle selection, swipe to set as default
 * (revealing [SetDefaultDismissActionRow]). On first launch the first row animates its swipe hint.
 * Selected rows (the default one included) get a faint primary tint over the base card color.
 */
@Composable
fun SwipeableScreenTimeoutCard(
    item: ScreenTimeoutUI,
    itemPosition: ItemPosition,
    modifier: Modifier = Modifier,
    swipeEnabled: Boolean = true,
    showFirstLaunchHint: Boolean,
    onFirstLaunchHintPlayed: (() -> Unit)? = null,
    onClickAction: ((ScreenTimeoutUI) -> Unit)?,
    onSwipeAction: ((SwipeToDismissBoxValue, ScreenTimeoutUI) -> Unit)?,
    content: @Composable (ScreenTimeoutUI?) -> Unit
) {
    val isDefault = item.isDefault
    val isFirst = itemPosition == ItemPosition.FIRST

    val containerColor by animateColorAsState(
        targetValue = CardDefaults.cardColors().containerColor,
        animationSpec = tween(SELECTED_ROW_TINT_ANIMATION_MS),
        label = "SelectedRowTint",
    )

    SwipeableItemCard(
        item = item,
        itemPosition = itemPosition,
        modifier = modifier,
        swipeEnabled = swipeEnabled,
        containerColor = containerColor,
        animateSwipeCondition = isDefault,
        animateFirstDisplayCondition = isFirst && showFirstLaunchHint,
        onFirstDisplayAnimationPlayed = onFirstLaunchHintPlayed,
        onClickAction = onClickAction,
        onSwipeAction = onSwipeAction,
        swipeThresholdFraction = SCREEN_TIMEOUT_CARD_SWIPE_THRESHOLD,
        backgroundContent = { dismissDirection, dismissProgress, currentItem ->
            if (currentItem != null) {
                SetDefaultDismissActionRow(
                    dismissDirection = dismissDirection,
                    dismissProgress = dismissProgress,
                    screenTimeoutUI = currentItem,
                    swipeEnabledState = swipeEnabled,
                    swipeThresholdFraction = SCREEN_TIMEOUT_CARD_SWIPE_THRESHOLD,
                )
            }
        },
        content = content
    )
}
