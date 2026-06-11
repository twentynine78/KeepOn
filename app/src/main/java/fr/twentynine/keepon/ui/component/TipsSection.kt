package fr.twentynine.keepon.ui.component

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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fr.twentynine.keepon.ui.theme.KeepOnCardElevation
import fr.twentynine.keepon.ui.theme.KeepOnCardShape
import fr.twentynine.keepon.ui.catalog.TipsInfo
import fr.twentynine.keepon.ui.catalog.TipsAction
import fr.twentynine.keepon.ui.event.MainUIEvent
import kotlinx.coroutines.launch

/**
 * The dismissible tips carousel on the Home screen: pages through the relevant [tipsList], each tip
 * offering its action and a dismiss control. Emits the corresponding [MainUIEvent]s through [onEvent].
 */
@Composable
fun TipsSection(
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
        val displayedList = currentPagerList
        val currentPage = pagerState.currentPage

        when {
            tipsList.isEmpty() -> currentPagerList = emptyList()

            tipsList == displayedList -> Unit

            else -> {
                // When the tip currently on screen is dismissed, slide to the neighbour that will take
                // its place BEFORE swapping the backing list, so the neighbour visibly slides in. The
                // keyed pager keeps that neighbour in view once the dismissed tip is removed.
                val currentTipRemoved = displayedList.getOrNull(currentPage)
                    ?.let { shown -> tipsList.none { it.id == shown.id } } == true

                if (currentTipRemoved && tipsList.size < displayedList.size && !pagerState.isScrollInProgress) {
                    val slideToPage = when {
                        currentPage < displayedList.lastIndex -> currentPage + 1 // right neighbour slides left into place
                        currentPage > 0 -> currentPage - 1 // last tip: left neighbour slides right into place
                        else -> -1 // it was the only tip (the section will just collapse)
                    }
                    if (slideToPage >= 0) {
                        pagerState.animateScrollToPage(slideToPage)
                    }
                }

                currentPagerList = tipsList
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
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = KeepOnCardElevation),
                        shape = KeepOnCardShape,
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(
                                    start = 14.dp,
                                    end = 14.dp,
                                    top = 14.dp,
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
                                    imageVector = tip.iconImageVector,
                                    contentDescription = stringResource(tip.titleId),
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
                                        onEvent(
                                            when (tip.buttonAction) {
                                                TipsAction.RequestPostNotification -> MainUIEvent.RequestPostNotification
                                                TipsAction.RequestAddTileService -> MainUIEvent.RequestAddTileService
                                                TipsAction.RequestAppRate -> MainUIEvent.RequestAppRate
                                            }
                                        )
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
        // A plain Row: at most a handful of static dots, no need for lazy machinery.
        Row(
            modifier = Modifier
                .wrapContentWidth()
                .wrapContentHeight()
                .padding(top = 8.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(pagerState.pageCount) { index ->
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
