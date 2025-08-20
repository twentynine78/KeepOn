package fr.twentynine.keepon.ui.view

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.lerp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import fr.twentynine.keepon.R
import fr.twentynine.keepon.data.enums.ItemPosition
import fr.twentynine.keepon.data.enums.TimeoutIconSize
import fr.twentynine.keepon.data.local.TipsInfo
import fr.twentynine.keepon.data.mapper.ScreenTimeoutUIToScreenTimeoutMapper
import fr.twentynine.keepon.data.model.MainUIEvent
import fr.twentynine.keepon.data.model.MainViewUIState
import fr.twentynine.keepon.data.model.ScreenTimeoutUI
import fr.twentynine.keepon.data.model.TimeoutIconData
import fr.twentynine.keepon.data.model.TimeoutIconStyle
import fr.twentynine.keepon.ui.util.KeepOnNavigationType
import fr.twentynine.keepon.ui.util.MAX_SCREEN_CONTENT_WIDTH_IN_DP
import kotlinx.coroutines.launch

private val ItemCardRoundedCornerShape = RoundedCornerShape(14.dp)
private const val ITEM_CARD_BACKGROUND_COLOR_ALPHA = 0.65f
private const val ITEM_CARD_BORDER_COLOR_ALPHA = 0.35f

@Composable
fun HomeView(
    uiState: MainViewUIState.Success,
    onEvent: (MainUIEvent) -> Unit,
    navType: KeepOnNavigationType,
) {
    HomeScreen(
        tipsList = uiState.tipsList,
        resetTimeoutWhenScreenOff = uiState.resetTimeoutWhenScreenOff,
        screenTimeouts = uiState.screenTimeouts,
        timeoutIconStyle = uiState.timeoutIconStyle,
        onEvent = onEvent,
        navType = navType,
    )
}

@Composable
fun HomeScreen(
    tipsList: List<TipsInfo>,
    resetTimeoutWhenScreenOff: Boolean,
    screenTimeouts: List<ScreenTimeoutUI>,
    timeoutIconStyle: TimeoutIconStyle,
    onEvent: (MainUIEvent) -> Unit,
    navType: KeepOnNavigationType,
) {
    val baseMaxWidthModifier = remember {
        Modifier
            .fillMaxHeight()
            .width(MAX_SCREEN_CONTENT_WIDTH_IN_DP.dp)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            TipsSectionView(tipsList = tipsList, onEvent = onEvent, modifier = baseMaxWidthModifier.padding(top = 8.dp))
        }

        item {
            KeepOnBehaviorCard(
                resetTimeoutWhenScreenOff = resetTimeoutWhenScreenOff,
                defaultScreenTimeoutUI = remember(
                    screenTimeouts.firstOrNull { it.isDefault },
                    screenTimeouts.firstOrNull()
                ) {
                    screenTimeouts.firstOrNull { it.isDefault } ?: screenTimeouts.first()
                },
                onEvent = onEvent,
                modifier = baseMaxWidthModifier,
            )
        }

        item {
            Column(
                modifier = baseMaxWidthModifier
                    .padding(top = 24.dp),
            ) {
                CardWithHeaderView(
                    icon = painterResource(R.drawable.ic_list_add),
                    iconSize = 22,
                    title = stringResource(R.string.select_timeouts_title),
                    descText = stringResource(R.string.select_timeouts_text),
                )
            }
        }

        itemsIndexed(
            items = screenTimeouts,
            key = { _, item -> item.value }
        ) { index, screenTimeout ->
            val itemPosition = remember(index, screenTimeouts.size) {
                ItemPosition.getItemPosition(index, screenTimeouts.size)
            }

            ScreenTimeoutRow(
                item = screenTimeout,
                itemPosition = itemPosition,
                timeoutIconStyle = timeoutIconStyle,
                resetTimeoutWhenScreenOff = resetTimeoutWhenScreenOff,
                onEvent = onEvent,
                modifier = baseMaxWidthModifier,
            )
        }

        item {
            val spacerBottomHeight = remember(navType) {
                when (navType) {
                    KeepOnNavigationType.BOTTOM_NAVIGATION -> 118.dp
                    else -> 12.dp
                }
            }
            Spacer(modifier = Modifier.padding(bottom = spacerBottomHeight))
        }
    }
}

@Composable
fun KeepOnBehaviorCard(
    resetTimeoutWhenScreenOff: Boolean,
    defaultScreenTimeoutUI: ScreenTimeoutUI,
    onEvent: (MainUIEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .padding(top = 28.dp, bottom = 12.dp),
    ) {
        CardWithHeaderView(
            icon = painterResource(R.drawable.ic_keepon),
            title = stringResource(R.string.general_behavior_title),
            descText = stringResource(R.string.general_behavior_text),
        )
        Card(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .align(alignment = Alignment.Start),
            shape = RoundedCornerShape(24.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = {
                        val newValue = !resetTimeoutWhenScreenOff
                        onEvent(MainUIEvent.SetResetTimeoutWhenScreenOff(newValue))
                    })
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Switch(
                    checked = resetTimeoutWhenScreenOff,
                    onCheckedChange = null,
                    modifier = Modifier.padding(end = 16.dp)
                )

                Text(
                    text = stringResource(
                        R.string.general_behavior_short_text,
                        defaultScreenTimeoutUI.displayName,
                    )
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ScreenTimeoutRow(
    item: ScreenTimeoutUI,
    itemPosition: ItemPosition,
    timeoutIconStyle: TimeoutIconStyle,
    resetTimeoutWhenScreenOff: Boolean,
    onEvent: (MainUIEvent) -> Unit,
    modifier: Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    val tooltipState = rememberTooltipState(isPersistent = true)

    val imageData = remember(timeoutIconStyle) {
        TimeoutIconData(
            ScreenTimeoutUIToScreenTimeoutMapper.map(item),
            TimeoutIconSize.MEDIUM,
            timeoutIconStyle
        )
    }

    SwipeableScreenTimeoutUICardView(
        modifier = modifier
            .fillMaxSize(),
        item = item,
        itemPosition = itemPosition,
        swipeEnabled = resetTimeoutWhenScreenOff,
        onClickAction = { clickedItem ->
            if (clickedItem.isLocked) {
                coroutineScope.launch { tooltipState.show() }
            } else {
                onEvent(MainUIEvent.ToggleScreenTimeoutSelection(clickedItem))
            }
        },
        onSwipeAction = { dismissedValue, dismissedItem ->
            onEvent(
                MainUIEvent.SetDefaultScreenTimeout(
                    ScreenTimeoutUIToScreenTimeoutMapper.map(
                        dismissedItem
                    )
                )
            )
        },
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Box(
                modifier = Modifier
                    .clip(ItemCardRoundedCornerShape)
                    .background(
                        MaterialTheme.colorScheme.surface.copy(
                            alpha = ITEM_CARD_BACKGROUND_COLOR_ALPHA
                        )
                    )
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(
                            alpha = ITEM_CARD_BORDER_COLOR_ALPHA
                        ),
                        shape = ItemCardRoundedCornerShape
                    )
                    .size(38.dp)
                    .align(Alignment.CenterStart),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    modifier = Modifier.size(20.dp, 20.dp),
                    model = imageData,
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant),
                    contentDescription = item.displayName,
                )
            }

            val animatedFontWeightValue by animateFloatAsState(
                targetValue = if (item.isCurrent) 1f else 0f,
                animationSpec = tween(600),
                label = "FontWeightAnimation"
            )

            Text(
                modifier = Modifier
                    .padding(start = 72.dp)
                    .align(Alignment.CenterStart),
                style = MaterialTheme.typography.labelMedium,
                text = item.displayName,
                fontSize = 17.sp,
                fontWeight = lerp(
                    start = FontWeight.Normal,
                    stop = FontWeight.ExtraBold,
                    fraction = animatedFontWeightValue
                ),
            )

            if (item.isLocked) {
                Box(
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .align(Alignment.CenterEnd),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    TooltipBox(
                        modifier = Modifier,
                        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                        tooltip = {
                            PlainTooltip(
                                modifier = Modifier
                                    .padding(8.dp),
                                contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                                containerColor = MaterialTheme.colorScheme.inverseSurface,
                                shadowElevation = 2.dp,
                            ) {
                                Text(
                                    modifier = Modifier
                                        .padding(4.dp),
                                    text = stringResource(R.string.timeout_locked_tooltips_text),
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                            }
                        },
                        state = tooltipState
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Lock,
                            contentDescription = stringResource(R.string.timeout_locked_icon_desc),
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .size(20.dp, 20.dp)
                                .clickable { coroutineScope.launch { tooltipState.show() } },
                        )
                    }
                }
            } else {
                Checkbox(
                    checked = item.isSelected,
                    enabled = !item.isDefault,
                    onCheckedChange = null,
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .align(Alignment.CenterEnd)
                )
            }
        }
    }
}
