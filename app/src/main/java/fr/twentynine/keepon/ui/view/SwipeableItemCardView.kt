package fr.twentynine.keepon.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxDefaults
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Density
import fr.twentynine.keepon.data.enums.ItemPosition
import fr.twentynine.keepon.ui.util.defaultCardHorizontalPadding
import fr.twentynine.keepon.ui.util.rememberBottomPadding
import fr.twentynine.keepon.ui.util.rememberCardShape
import fr.twentynine.keepon.ui.util.rememberItemBottomBorderPadding
import fr.twentynine.keepon.ui.util.rememberTopPadding

const val DEFAULT_SWIPE_THRESHOLD_FRACTION = 0.30f

private val infiniteDensity = Density(Float.POSITIVE_INFINITY)

@Composable
private fun rememberNoFlingSwipeToDismissBoxState(
    initialValue: SwipeToDismissBoxValue = SwipeToDismissBoxValue.Settled,
    confirmValueChange: (SwipeToDismissBoxValue) -> Boolean = { true },
    positionalThreshold: (totalDistance: Float) -> Float =
        SwipeToDismissBoxDefaults.positionalThreshold,
): SwipeToDismissBoxState {
    return rememberSaveable(
        saver = SwipeToDismissBoxState.Saver(
            confirmValueChange = confirmValueChange,
            density = infiniteDensity,
            positionalThreshold = positionalThreshold
        )
    ) {
        SwipeToDismissBoxState(initialValue, infiniteDensity, confirmValueChange, positionalThreshold)
    }
}

@Composable
fun <T> SwipeableItemCardView(
    item: T,
    itemPosition: ItemPosition,
    modifier: Modifier = Modifier,
    swipeEnabled: Boolean = true,
    onClickAction: ((T) -> Unit)?,
    onSwipeAction: ((SwipeToDismissBoxValue, T) -> Unit)?,
    swipeThresholdFraction: Float = DEFAULT_SWIPE_THRESHOLD_FRACTION,
    backgroundContent:
    @Composable ((dismissDirection: SwipeToDismissBoxValue, dismissProgress: Float, currentItem: T) -> Unit)?,
    content: @Composable (T) -> Unit,
) {
    val topPadding = rememberTopPadding(itemPosition = itemPosition)
    val bottomPadding = rememberBottomPadding(itemPosition = itemPosition)
    val itemBottomBorderPadding = rememberItemBottomBorderPadding(itemPosition = itemPosition)
    val shape = rememberCardShape(itemPosition = itemPosition)

    val state = rememberNoFlingSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue != SwipeToDismissBoxValue.Settled && onSwipeAction != null) {
                onSwipeAction(dismissValue, item)
            }
            false
        },
        positionalThreshold = { it * swipeThresholdFraction }
    )

    val cardContainerColor = CardDefaults.cardColors().containerColor

    val cardModifier = if (onClickAction != null) {
        Modifier.clickable { onClickAction(item) }
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .padding(
                start = defaultCardHorizontalPadding,
                end = defaultCardHorizontalPadding,
                top = topPadding,
                bottom = bottomPadding
            )
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = itemBottomBorderPadding),
            colors = CardDefaults.cardColors().copy(containerColor = Color.Transparent),
            shape = shape,
        ) {
            SwipeToDismissBox(
                modifier = cardModifier,
                state = state,
                enableDismissFromEndToStart = swipeEnabled && onSwipeAction != null && backgroundContent != null,
                enableDismissFromStartToEnd = swipeEnabled && onSwipeAction != null && backgroundContent != null,
                backgroundContent = {
                    if (backgroundContent != null) {
                        backgroundContent(state.dismissDirection, state.progress, item)
                    }
                },
                content = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(cardContainerColor, shape),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        content(item)
                    }
                }
            )
        }
    }
}
