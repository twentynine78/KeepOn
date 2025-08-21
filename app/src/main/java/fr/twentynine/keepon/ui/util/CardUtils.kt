package fr.twentynine.keepon.ui.util

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import fr.twentynine.keepon.data.enums.ItemPosition

val defaultRoundedCornerSize = 24.dp
val defaultBorderWidth = 1.dp
val defaultCardHorizontalPadding = 16.dp
val itemPaddingTopFirst = 8.dp
val itemPaddingBottomLast = 12.dp
val itemPaddingDefault = 0.dp

@Composable
fun rememberTopPadding(itemPosition: ItemPosition): Dp {
    return remember(itemPosition) {
        when (itemPosition) {
            ItemPosition.FIRST, ItemPosition.FIRST_AND_LAST -> itemPaddingTopFirst
            else -> itemPaddingDefault
        }
    }
}

@Composable
fun rememberBottomPadding(itemPosition: ItemPosition): Dp {
    return remember(itemPosition) {
        when (itemPosition) {
            ItemPosition.LAST, ItemPosition.FIRST_AND_LAST -> itemPaddingBottomLast
            else -> itemPaddingDefault
        }
    }
}

@Composable
fun rememberItemBottomBorderPadding(itemPosition: ItemPosition): Dp {
    return remember(itemPosition) {
        when (itemPosition) {
            ItemPosition.LAST, ItemPosition.FIRST_AND_LAST -> itemPaddingDefault
            else -> defaultBorderWidth
        }
    }
}

@Composable
fun rememberCardShape(itemPosition: ItemPosition): RoundedCornerShape {
    return remember(itemPosition) {
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
}
