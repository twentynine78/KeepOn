package fr.twentynine.keepon.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fr.twentynine.keepon.ui.theme.KeepOnCardElevation
import fr.twentynine.keepon.ui.theme.KeepOnCardShape
import fr.twentynine.keepon.ui.catalog.TipInfo
import fr.twentynine.keepon.ui.catalog.TipAction
import fr.twentynine.keepon.ui.event.MainUIEvent
import kotlinx.coroutines.launch

/** How much of the error tone to blend into a warning tip's amber container/content (0 = none). */
private const val WARNING_TONE_BLEND = 0.7f

/**
 * The dismissible tips carousel on the Home screen: pages through the relevant [tipsList], each tip
 * offering its action and a dismiss control. Emits the corresponding [MainUIEvent]s through [onEvent].
 */
@Composable
fun TipsSection(
    tipsList: List<TipInfo>,
    onEvent: (MainUIEvent) -> Unit,
    modifier: Modifier = Modifier,
    emphasisTick: Int = 0,
) {
    val coroutineScope = rememberCoroutineScope()
    var currentPagerList by remember { mutableStateOf(tipsList) }
    val pagerState = rememberPagerState(pageCount = { currentPagerList.size })
    val pagerPadding = remember(pagerState.pageCount) {
        if (pagerState.pageCount > 1) 4.dp else 8.dp
    }

    // Bumped when the FAB is tapped with nothing selected: bring the (always-first) warning tip into
    // view and give the carousel a brief scale pulse. Guarded so it never fires on initial composition.
    val emphasisScale = remember { Animatable(1f) }
    LaunchedEffect(emphasisTick) {
        if (emphasisTick > 0) {
            pagerState.animateScrollToPage(0)
            emphasisScale.animateTo(1.04f, tween(150))
            emphasisScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        }
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
                .graphicsLayer {
                    scaleX = emphasisScale.value
                    scaleY = emphasisScale.value
                }
                .padding(top = 8.dp, bottom = pagerPadding),
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
                    TipCard(tip = tip, onEvent = onEvent)
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

/**
 * A single tip card: pulsing icon + title, body, and an optional button row. A [TipInfo.isWarning] tip
 * blends a touch of the error tone into the usual amber so it reads as more urgent than an ordinary
 * tip, without leaving the palette.
 */
@Composable
private fun TipCard(
    tip: TipInfo,
    onEvent: (MainUIEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val containerColor = if (tip.isWarning) {
        lerp(colorScheme.tertiaryContainer, colorScheme.errorContainer, WARNING_TONE_BLEND)
    } else {
        colorScheme.tertiaryContainer
    }
    val contentColor = if (tip.isWarning) {
        lerp(colorScheme.onTertiaryContainer, colorScheme.onErrorContainer, WARNING_TONE_BLEND)
    } else {
        colorScheme.onTertiaryContainer
    }

    ElevatedCard(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = KeepOnCardElevation),
        shape = KeepOnCardShape,
    ) {
        Column(
            modifier = Modifier
                .padding(start = 14.dp, end = 14.dp, top = 14.dp, bottom = 8.dp),
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

            val hasButtons = tip.buttonDismissTextId != null ||
                (tip.buttonAction != null && tip.buttonTextId != null)

            Text(
                text = stringResource(tip.textId),
                modifier = if (hasButtons) Modifier else Modifier.padding(bottom = 6.dp),
            )

            if (hasButtons) {
                TipButtons(tip = tip, onEvent = onEvent)
            }
        }
    }
}

/**
 * The trailing button row of a [TipCard]: a dismiss text button and/or a primary action button. A tip
 * can omit either (or both, e.g. the functional [TipInfo.SelectTimeout] advisory); the locals are
 * captured so each id/action is smart-cast where it is rendered.
 */
@Composable
private fun TipButtons(
    tip: TipInfo,
    onEvent: (MainUIEvent) -> Unit,
) {
    val dismissTextId = tip.buttonDismissTextId
    val actionTextId = tip.buttonTextId
    val action = tip.buttonAction

    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (dismissTextId != null) {
            TextButton(
                onClick = { onEvent(MainUIEvent.DismissTip(tip.id)) },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                ),
            ) {
                Text(
                    text = stringResource(dismissTextId),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Normal,
                )
            }
        }
        if (action != null && actionTextId != null) {
            TextButton(
                onClick = { onEvent(action.toUIEvent()) },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                ),
            ) {
                Text(
                    text = stringResource(actionTextId),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

/** Maps a tip's primary [TipAction] to the [MainUIEvent] the screen should emit. */
private fun TipAction.toUIEvent(): MainUIEvent = when (this) {
    TipAction.RequestPostNotification -> MainUIEvent.RequestPostNotification
    TipAction.RequestAddTileService -> MainUIEvent.RequestAddTileService
    TipAction.RequestAppRate -> MainUIEvent.RequestAppRate
}
