package fr.twentynine.keepon.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import fr.twentynine.keepon.ui.model.ItemPosition
import fr.twentynine.keepon.ui.theme.KeepOnCardElevation
import fr.twentynine.keepon.ui.util.bottomPaddingFor
import fr.twentynine.keepon.ui.util.cardShapeFor
import fr.twentynine.keepon.ui.util.defaultCardHorizontalPadding
import fr.twentynine.keepon.ui.util.itemBottomBorderPaddingFor
import fr.twentynine.keepon.ui.util.topPaddingFor

/**
 * A list card positioned within a vertical group: applies the rounded-corner shape and the
 * top/bottom paddings for its [ItemPosition], over the standard card background. Used directly by
 * static cards ([ItemCard]); [SwipeableItemCard] layers swipe-to-dismiss on the same structure.
 */
@Composable
fun PositionedCard(
    itemPosition: ItemPosition,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val topPadding = topPaddingFor(itemPosition)
    val bottomPadding = bottomPaddingFor(itemPosition)
    val itemBottomBorderPadding = itemBottomBorderPaddingFor(itemPosition)
    val shape = cardShapeFor(itemPosition)
    val cardContainerColor = CardDefaults.cardColors().containerColor

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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(cardContainerColor, shape),
                contentAlignment = Alignment.CenterStart,
                content = content,
            )
        }
    }
}
