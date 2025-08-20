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

private val defaultRoundedCornerSize = 24.dp
private val defaultBorderWidth = 1.dp
private val defaultBoxVerticalPadding = 16.dp
private val ItemPaddingTopFirst = 8.dp
private val ItemPaddingBottomLast = 12.dp
private val ItemPaddingDefault = 0.dp

@Composable
fun ItemCardView(
    itemPosition: ItemPosition,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val topPadding: Dp = remember(itemPosition) {
        when (itemPosition) {
            ItemPosition.FIRST, ItemPosition.FIRST_AND_LAST -> ItemPaddingTopFirst
            else -> ItemPaddingDefault
        }
    }

    val bottomPadding: Dp = remember(itemPosition) {
        when (itemPosition) {
            ItemPosition.LAST, ItemPosition.FIRST_AND_LAST -> ItemPaddingBottomLast
            else -> ItemPaddingDefault
        }
    }

    val itemBottomBorderPadding: Dp = remember(itemPosition) {
        when (itemPosition) {
            ItemPosition.LAST, ItemPosition.FIRST_AND_LAST -> ItemPaddingDefault
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

    val borderColor: Color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.1f)

    Box(
        modifier = modifier
            .padding(
                start = defaultBoxVerticalPadding,
                end = defaultBoxVerticalPadding,
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
