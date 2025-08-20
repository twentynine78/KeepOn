package fr.twentynine.keepon.ui.view

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color // Added for Color type
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp // Added for Dp type
import androidx.compose.ui.unit.dp
import fr.twentynine.keepon.R
import fr.twentynine.keepon.data.enums.ItemPosition
import fr.twentynine.keepon.data.model.ScreenTimeoutUI

private const val SWIPE_THRESHOLD_FRACTION = 0.25f
private const val CONTENT_COLOR_ANIMATION_DURATION_MS = 200
private const val BACKGROUND_COLOR_ANIMATION_DURATION_MS = 400

private val defaultRoundedCornerSize = 24.dp
private val defaultBorderWidth = 1.dp
private val defaultBoxHorizontalPadding = 16.dp
private val itemPaddingTopFirst = 8.dp
private val itemPaddingBottomLast = 12.dp
private val itemPaddingDefault = 0.dp

@Composable
fun SwipeableScreenTimeoutUICardView(
    item: ScreenTimeoutUI,
    itemPosition: ItemPosition,
    modifier: Modifier = Modifier,
    swipeEnabled: Boolean = true,
    onClickAction: (ScreenTimeoutUI) -> Unit,
    onSwipeAction: (SwipeToDismissBoxValue, ScreenTimeoutUI) -> Unit,
    content: @Composable (ScreenTimeoutUI) -> Unit
) {
    val topPadding: Dp = remember(itemPosition) {
        when (itemPosition) {
            ItemPosition.FIRST, ItemPosition.FIRST_AND_LAST -> itemPaddingTopFirst
            else -> itemPaddingDefault
        }
    }

    val bottomPadding: Dp = remember(itemPosition) {
        when (itemPosition) {
            ItemPosition.LAST, ItemPosition.FIRST_AND_LAST -> itemPaddingBottomLast
            else -> itemPaddingDefault
        }
    }

    val itemBottomBorderPadding: Dp = remember(itemPosition) {
        when (itemPosition) {
            ItemPosition.LAST, ItemPosition.FIRST_AND_LAST -> itemPaddingDefault
            else -> defaultBorderWidth
        }
    }

    val shape: RoundedCornerShape = remember(itemPosition) {
        when (itemPosition) {
            ItemPosition.FIRST -> RoundedCornerShape(
                topStart = defaultRoundedCornerSize,
                topEnd = defaultRoundedCornerSize,
            )
            ItemPosition.LAST -> RoundedCornerShape(
                bottomStart = defaultRoundedCornerSize,
                bottomEnd = defaultRoundedCornerSize,
            )
            ItemPosition.FIRST_AND_LAST -> RoundedCornerShape(
                size = defaultRoundedCornerSize
            )
            else -> RoundedCornerShape(0.dp)
        }
    }

    val state = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue != SwipeToDismissBoxValue.Settled) {
                onSwipeAction(dismissValue, item)
            }
            false
        },
        positionalThreshold = { it * SWIPE_THRESHOLD_FRACTION }
    )

    val backgroundEnabledColor = MaterialTheme.colorScheme.primaryContainer
    val backgroundDisabledColor = MaterialTheme.colorScheme.outlineVariant
    val contentEnabledTint = MaterialTheme.colorScheme.onPrimaryContainer
    val contentDisabledTint = MaterialTheme.colorScheme.outline
    val enabledText = stringResource(R.string.select_timeouts_swipe_set_default_text)
    val disabledText = stringResource(R.string.select_timeouts_swipe_already_default_text)

    val textSetDefault = remember(item.isDefault, enabledText, disabledText) {
        if (!item.isDefault) enabledText else disabledText
    }

    val iconSetDefault = remember(item.isDefault) {
        if (!item.isDefault) Icons.Rounded.Build else Icons.Rounded.Done
    }
    val contentTint = remember(item.isDefault) {
        val finalColor = if (!item.isDefault) contentEnabledTint else contentDisabledTint
        finalColor.copy(alpha = 0.5f)
    }
    val backgroundColor = remember(item.isDefault, state.progress) {
        if (!item.isDefault) backgroundEnabledColor else backgroundDisabledColor
    }
    val animatedBackgroundColor by animateColorAsState(
        targetValue = backgroundColor,
        animationSpec = tween(durationMillis = BACKGROUND_COLOR_ANIMATION_DURATION_MS),
        label = "backgroundColorAnimation"
    )

    val animatedContentColor by animateColorAsState(
        targetValue = contentTint.copy(
            alpha = if (item.isDefault || state.progress == 1f || state.progress < SWIPE_THRESHOLD_FRACTION) 0.5f else 1f
        ),
        animationSpec = tween(durationMillis = CONTENT_COLOR_ANIMATION_DURATION_MS),
        label = "contentColorAnimation"
    )

    Box(
        modifier = modifier
            .padding(
                start = defaultBoxHorizontalPadding,
                end = defaultBoxHorizontalPadding,
                top = topPadding,
                bottom = bottomPadding
            )
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = itemBottomBorderPadding),
            colors = CardDefaults.cardColors(),
            shape = shape,
        ) {
            SwipeToDismissBox(
                modifier = Modifier
                    .clickable { onClickAction(item) },
                state = state,
                enableDismissFromEndToStart = swipeEnabled,
                enableDismissFromStartToEnd = swipeEnabled,
                backgroundContent = {
                    DismissBackground(
                        dismissDirection = state.dismissDirection,
                        dismissProgress = state.progress,
                        icon = iconSetDefault,
                        text = textSetDefault,
                        contentTint = animatedContentColor,
                        backgroundColor = animatedBackgroundColor,
                    )
                },
                content = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(CardDefaults.cardColors().containerColor, shape),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        content(item)
                    }
                }
            )
        }
    }
}

@Composable
fun DismissBackground(
    dismissDirection: SwipeToDismissBoxValue,
    dismissProgress: Float,
    icon: ImageVector,
    text: String,
    contentTint: Color,
    backgroundColor: Color,
) {
    val showContent = dismissDirection == SwipeToDismissBoxValue.EndToStart ||
        dismissDirection == SwipeToDismissBoxValue.StartToEnd

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = when (dismissDirection) {
            SwipeToDismissBoxValue.EndToStart -> Arrangement.End
            SwipeToDismissBoxValue.StartToEnd -> Arrangement.Start
            else -> Arrangement.Center
        },
    ) {
        if (dismissProgress != 1f && showContent) {
            val iconComponent = @Composable {
                Icon(
                    modifier = Modifier.padding(horizontal = 18.dp),
                    imageVector = icon,
                    tint = contentTint,
                    contentDescription = null,
                )
            }
            val textComponent = @Composable {
                Text(
                    text = text,
                    color = contentTint,
                )
            }

            if (dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                textComponent()
                iconComponent()
            } else {
                iconComponent()
                textComponent()
            }
        }
    }
}
