package fr.twentynine.keepon.ui.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults // Changed
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fr.twentynine.keepon.data.local.TipsInfo
import fr.twentynine.keepon.data.model.MainUIEvent
import fr.twentynine.keepon.ui.util.PulsatingIcon
import kotlinx.coroutines.launch

@Composable
fun TipsSectionView(
    tipsList: List<TipsInfo>,
    onEvent: (MainUIEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    var currentPagerList by remember { mutableStateOf(tipsList) }
    val pagerState = rememberPagerState(pageCount = { currentPagerList.size })
    val pagerPadding = remember(pagerState.pageCount) {
        if (pagerState.pageCount > 1) 4.dp else 12.dp
    }

    LaunchedEffect(tipsList, pagerState.settledPage) {
        val newIncomingList = tipsList
        val currentlyDisplayedList = currentPagerList
        val currentPageInDisplayedList = pagerState.currentPage

        if (newIncomingList.isEmpty()) {
            currentPagerList = emptyList()
        } else {
            if (newIncomingList != currentlyDisplayedList) {
                val oldPageCount = currentlyDisplayedList.size
                val newPageCount = newIncomingList.size

                var targetPage = currentPageInDisplayedList

                if (newPageCount < oldPageCount) {
                    val currentItemId = if (currentPageInDisplayedList < currentlyDisplayedList.size) {
                        currentlyDisplayedList.getOrNull(currentPageInDisplayedList)?.id
                    } else {
                        null
                    }

                    if (currentItemId != null && newIncomingList.none { it.id == currentItemId }) {
                        targetPage = if (currentPageInDisplayedList >= newPageCount) {
                            (newPageCount - 1).coerceAtLeast(0)
                        } else {
                            currentPageInDisplayedList
                        }
                    } else if (currentPageInDisplayedList >= newPageCount) {
                        targetPage = (newPageCount - 1).coerceAtLeast(0)
                    }
                }
                currentPagerList = newIncomingList

                if (targetPage != pagerState.currentPage && targetPage < newIncomingList.size && !pagerState.isScrollInProgress) {
                    if (pagerState.currentPage != targetPage) {
                        if (targetPage < currentPagerList.size) {
                            pagerState.animateScrollToPage(targetPage)
                        }
                    }
                }
            }
        }
    }

    AnimatedVisibility(
        visible = currentPagerList.isNotEmpty(),
        enter = expandVertically(),
        exit = shrinkVertically(),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = modifier
                .animateContentSize()
                .padding(top = 12.dp, bottom = pagerPadding),
        ) {
            HorizontalPager(
                state = pagerState,
                key = { index -> currentPagerList[index].id },
                verticalAlignment = Alignment.Top,
                modifier = Modifier
                    .wrapContentHeight()
                    .padding(top = 8.dp, bottom = 4.dp),
            ) { currentPageIndex ->
                val tipsInfo = remember(currentPageIndex, currentPagerList) {
                    currentPagerList.getOrNull(currentPageIndex)
                }

                tipsInfo?.let { tip ->
                    ElevatedCard(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        ),
                        shape = RoundedCornerShape(24.dp),
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(
                                    start = 16.dp,
                                    end = 16.dp,
                                    top = 16.dp,
                                    bottom = 8.dp
                                ),
                            horizontalAlignment = Alignment.Start,
                            verticalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(bottom = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Start,
                            ) {
                                val infiniteTransition = rememberInfiniteTransition(label = "pulsatingIconTransition")

                                PulsatingIcon(
                                    infiniteTransition = infiniteTransition,
                                    initialSize = 14f,
                                    painter = painterResource(tip.iconResourceId),
                                    contentDescription = stringResource(tip.titleId),
                                    modifier = Modifier
                                )
                                Text(
                                    text = stringResource(tip.titleId),
                                    style = MaterialTheme.typography.titleSmall,
                                    modifier = Modifier
                                        .padding(start = 8.dp)
                                )
                            }
                            Text(text = stringResource(tip.textId))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = { onEvent(MainUIEvent.DismissTips(tip.id)) },
                                    colors = ButtonDefaults.textButtonColors(
                                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                                    ),
                                ) {
                                    Text(
                                        text = stringResource(tip.buttonDismissTextId),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Normal,
                                    )
                                }
                                TextButton(
                                    onClick = {
                                        onEvent(tip.buttonAction)
                                    },
                                    colors = ButtonDefaults.textButtonColors(
                                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                                    ),
                                ) {
                                    Text(
                                        text = stringResource(tip.buttonTextId),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    AnimatedVisibility(
        visible = pagerState.pageCount > 1,
        enter = expandVertically(),
        exit = shrinkVertically(),
    ) {
        LazyRow(
            modifier = Modifier
                .wrapContentWidth()
                .wrapContentHeight()
                .padding(top = 8.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(pagerState.pageCount) { index ->
                val isSelected = pagerState.currentPage == index
                val color = if (isSelected) {
                    MaterialTheme.colorScheme.onBackground
                } else {
                    MaterialTheme.colorScheme.outline
                }

                val itemWidth by animateDpAsState(
                    targetValue = if (isSelected) 12.dp else 8.dp,
                    label = "pageIndicatorWidth",
                    animationSpec = tween(durationMillis = 400)
                )

                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(width = itemWidth, height = 8.dp)
                        .clip(CircleShape)
                        .background(color)
                        .clickable {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        }
                )
            }
        }
    }
}
