package fr.twentynine.keepon.ui.view

import android.os.Parcelable
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import fr.twentynine.keepon.data.enums.ItemPosition
import fr.twentynine.keepon.ui.util.defaultCardHorizontalPadding
import fr.twentynine.keepon.ui.util.rememberBottomPadding
import fr.twentynine.keepon.ui.util.rememberCardShape
import fr.twentynine.keepon.ui.util.rememberItemBottomBorderPadding
import fr.twentynine.keepon.ui.util.rememberTopPadding
import kotlinx.coroutines.delay

const val DEFAULT_SWIPE_THRESHOLD_FRACTION = 0.30f

private const val INITIAL_ANIMATION_DURATION = 400
private const val INITIAL_ANIMATION_DELAY = 450L
private const val SWIPE_ANIMATION_DELAY = 150L
private const val INITIAL_ANIMATION_OFFSET_DP = 64

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
private fun <T : Parcelable> AnimateSwipeableItemCardEffect(
    item: T?,
    animateSwipeCondition: Boolean = false,
    animateFirstDisplayCondition: Boolean = false,
    swipeToDismissDirection: SwipeToDismissBoxValue,
    animatedTranslationX: Animatable<Float, AnimationVector1D>,
) {
    val density = LocalDensity.current

    var isFirstEverAnimationCycleForThisItem by rememberSaveable(item) {
        mutableStateOf(true)
    }
    var animationTriggerHandled by rememberSaveable(animateSwipeCondition, animateFirstDisplayCondition) {
        mutableStateOf(false)
    }
    var originalFirstDisplayCondition by rememberSaveable {
        mutableStateOf(animateSwipeCondition || animateFirstDisplayCondition)
    }

    LaunchedEffect(item, animateSwipeCondition, animateFirstDisplayCondition) {
        if (item == null) {
            animatedTranslationX.snapTo(0f)
            return@LaunchedEffect
        }

        if ((!animateSwipeCondition && !animateFirstDisplayCondition)) {
            animatedTranslationX.snapTo(0f)
            return@LaunchedEffect
        }

        if (animationTriggerHandled) {
            animatedTranslationX.snapTo(0f)
            return@LaunchedEffect
        }

        val initialTranslationPx = with(density) { INITIAL_ANIMATION_OFFSET_DP.dp.toPx() }
        var playSnapBackAnimation = true
        var snapBackDampingRatio = Spring.DampingRatioMediumBouncy
        val springStiffness = Spring.StiffnessMediumLow

        if (isFirstEverAnimationCycleForThisItem) {
            isFirstEverAnimationCycleForThisItem = false

            if (animateFirstDisplayCondition && originalFirstDisplayCondition) {
                originalFirstDisplayCondition = false
                // First display animation sequence
                animatedTranslationX.snapTo(0f)
                animatedTranslationX.animateTo(
                    targetValue = initialTranslationPx + (initialTranslationPx / 2),
                    animationSpec = tween(INITIAL_ANIMATION_DURATION)
                )
                delay(INITIAL_ANIMATION_DELAY)
                snapBackDampingRatio = Spring.DampingRatioHighBouncy
            } else {
                if (animateSwipeCondition && !originalFirstDisplayCondition) {
                    originalFirstDisplayCondition = false
                    // Swipe animation sequence.
                    val offset = if (swipeToDismissDirection == SwipeToDismissBoxValue.EndToStart) {
                        initialTranslationPx * -1
                    } else {
                        initialTranslationPx
                    }
                    animatedTranslationX.snapTo(offset)
                    delay(SWIPE_ANIMATION_DELAY)
                } else {
                    originalFirstDisplayCondition = false
                    // No animation at all (neither first display nor swipe-in).
                    animatedTranslationX.snapTo(0f)
                    playSnapBackAnimation = false
                }
            }

            // Subsequent "Snap Back" or "Return" animation
            if (playSnapBackAnimation) {
                animatedTranslationX.animateTo(
                    targetValue = 0f,
                    animationSpec = spring(
                        dampingRatio = snapBackDampingRatio,
                        stiffness = springStiffness
                    )
                )
            } else {
                animatedTranslationX.snapTo(0f)
            }
            animationTriggerHandled = true
        }
    }
}

@Composable
fun <T : Parcelable> SwipeableItemCardView(
    item: T?,
    itemPosition: ItemPosition,
    modifier: Modifier = Modifier,
    swipeEnabled: Boolean = true,
    animateSwipeCondition: Boolean = false,
    animateFirstDisplayCondition: Boolean = false,
    onClickAction: ((T) -> Unit)?,
    onSwipeAction: ((SwipeToDismissBoxValue, T) -> Unit)?,
    swipeThresholdFraction: Float = DEFAULT_SWIPE_THRESHOLD_FRACTION,
    backgroundContent:
    @Composable ((dismissDirection: SwipeToDismissBoxValue, dismissProgress: Float, currentItem: T?) -> Unit)?,
    content: @Composable (T?) -> Unit,
) {
    val topPadding = rememberTopPadding(itemPosition = itemPosition)
    val bottomPadding = rememberBottomPadding(itemPosition = itemPosition)
    val itemBottomBorderPadding = rememberItemBottomBorderPadding(itemPosition = itemPosition)
    val shape = rememberCardShape(itemPosition = itemPosition)

    val swipeToDismissState = rememberNoFlingSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue != SwipeToDismissBoxValue.Settled && onSwipeAction != null && item != null) {
                onSwipeAction(dismissValue, item)
            }
            false
        },
        positionalThreshold = { it * swipeThresholdFraction }
    )

    val cardContainerColor = CardDefaults.cardColors().containerColor

    val cardModifier = remember(onClickAction, item) {
        if (onClickAction != null && item != null) {
            Modifier.clickable { onClickAction(item) }
        } else {
            Modifier
        }
    }

    val animatedTranslationX = remember { Animatable(0f) }
    val playAnimation = remember(swipeEnabled, backgroundContent) { swipeEnabled && backgroundContent != null }

    if (playAnimation) {
        AnimateSwipeableItemCardEffect(
            item = item,
            animateSwipeCondition = animateSwipeCondition,
            animateFirstDisplayCondition = animateFirstDisplayCondition,
            swipeToDismissDirection = swipeToDismissState.dismissDirection,
            animatedTranslationX = animatedTranslationX,
        )
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
                state = swipeToDismissState,
                enableDismissFromEndToStart = swipeEnabled && onSwipeAction != null && backgroundContent != null,
                enableDismissFromStartToEnd = swipeEnabled && onSwipeAction != null && backgroundContent != null,
                backgroundContent = {
                    backgroundContent?.invoke(swipeToDismissState.dismissDirection, swipeToDismissState.progress, item)
                },
                content = {
                    Box(
                        modifier = Modifier
                            .graphicsLayer {
                                translationX = animatedTranslationX.value
                            }
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
