package fr.twentynine.keepon.ui.util

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import fr.twentynine.keepon.ui.theme.KeepOnCardCornerRadius
import fr.twentynine.keepon.ui.model.ItemPosition

val defaultRoundedCornerSize = KeepOnCardCornerRadius
val defaultBorderWidth = 1.dp
val defaultCardHorizontalPadding = 16.dp
val itemPaddingTopFirst = 0.dp
val itemPaddingBottomLast = 12.dp
val itemPaddingDefault = 0.dp

// The four shapes a positioned list card can take, precomputed once.
private val firstCardShape = RoundedCornerShape(
    topStart = defaultRoundedCornerSize,
    topEnd = defaultRoundedCornerSize,
)
private val lastCardShape = RoundedCornerShape(
    bottomStart = defaultRoundedCornerSize,
    bottomEnd = defaultRoundedCornerSize,
)
private val singleCardShape = RoundedCornerShape(size = defaultRoundedCornerSize)
private val middleCardShape = RoundedCornerShape(0.dp)

fun topPaddingFor(itemPosition: ItemPosition): Dp = when (itemPosition) {
    ItemPosition.FIRST, ItemPosition.FIRST_AND_LAST -> itemPaddingTopFirst
    else -> itemPaddingDefault
}

fun bottomPaddingFor(itemPosition: ItemPosition): Dp = when (itemPosition) {
    ItemPosition.LAST, ItemPosition.FIRST_AND_LAST -> itemPaddingBottomLast
    else -> itemPaddingDefault
}

fun itemBottomBorderPaddingFor(itemPosition: ItemPosition): Dp = when (itemPosition) {
    ItemPosition.LAST, ItemPosition.FIRST_AND_LAST -> itemPaddingDefault
    else -> defaultBorderWidth
}

fun cardShapeFor(itemPosition: ItemPosition): RoundedCornerShape = when (itemPosition) {
    ItemPosition.FIRST -> firstCardShape
    ItemPosition.LAST -> lastCardShape
    ItemPosition.FIRST_AND_LAST -> singleCardShape
    else -> middleCardShape
}
