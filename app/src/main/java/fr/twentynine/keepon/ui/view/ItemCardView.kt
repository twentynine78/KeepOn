package fr.twentynine.keepon.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color // Added for Color type
import androidx.compose.ui.unit.Dp // Added for Dp type
import androidx.compose.ui.unit.dp
import fr.twentynine.keepon.data.enums.ItemPosition

@Composable
fun ItemCardView(
    itemPosition: ItemPosition,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val roundedCornerSize: Dp = remember { 24.dp }
    val borderWidth: Dp = remember { 1.dp }
    val boxVerticalPadding: Dp = remember { 16.dp }

    val topPadding: Dp = remember(itemPosition) {
        when (itemPosition) {
            ItemPosition.FIRST, ItemPosition.FIRST_AND_LAST -> 8.dp
            else -> 0.dp
        }
    }

    val bottomPadding: Dp = remember(itemPosition) {
        when (itemPosition) {
            ItemPosition.LAST, ItemPosition.FIRST_AND_LAST -> 12.dp
            else -> 0.dp
        }
    }

    val itemBottomBorderPadding: Dp = remember(itemPosition, borderWidth) { // Added borderWidth as a key
        when (itemPosition) {
            ItemPosition.LAST, ItemPosition.FIRST_AND_LAST -> 0.dp
            else -> borderWidth
        }
    }

    val shape: RoundedCornerShape = remember(itemPosition, roundedCornerSize) { // Added roundedCornerSize as a key
        when (itemPosition) {
            ItemPosition.FIRST -> RoundedCornerShape(
                topStart = roundedCornerSize,
                topEnd = roundedCornerSize,
            )
            ItemPosition.LAST -> RoundedCornerShape(
                bottomStart = roundedCornerSize,
                bottomEnd = roundedCornerSize,
            )
            ItemPosition.FIRST_AND_LAST -> RoundedCornerShape(
                size = roundedCornerSize
            )
            else -> RoundedCornerShape(0.dp)
        }
    }

    val borderColor: Color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.1f)

    Box(
        modifier = modifier
            .padding(
                start = boxVerticalPadding,
                end = boxVerticalPadding,
                top = topPadding,
                bottom = bottomPadding
            )
            .background(borderColor, shape)
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = itemBottomBorderPadding),
            colors = CardDefaults.cardColors(),
            shape = shape,
        ) {
            content()
        }
    }
}
