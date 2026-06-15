package fr.twentynine.keepon.ui.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.PlaylistAddCheck
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import fr.twentynine.keepon.R
import fr.twentynine.keepon.ui.model.ItemPosition
import fr.twentynine.keepon.ui.catalog.TipInfo
import fr.twentynine.keepon.ui.mapper.ScreenTimeoutUIToScreenTimeoutMapper
import fr.twentynine.keepon.ui.event.MainUIEvent
import fr.twentynine.keepon.ui.state.MainViewUIState
import fr.twentynine.keepon.ui.model.ScreenTimeoutUI
import fr.twentynine.keepon.domain.model.TimeoutIconStyle
import fr.twentynine.keepon.ui.component.TimeoutRowLabel
import fr.twentynine.keepon.ui.util.KeepOnNavigationType
import fr.twentynine.keepon.ui.util.bottomSpacerHeight
import fr.twentynine.keepon.ui.util.screenContentModifier
import fr.twentynine.keepon.ui.theme.KeepOnCardElevation
import fr.twentynine.keepon.ui.theme.KeepOnCardShape
import fr.twentynine.keepon.ui.theme.StyleTopSwitchRowVerticalPadding
import fr.twentynine.keepon.ui.component.CardHeader
import fr.twentynine.keepon.ui.component.rememberBehaviorSwitchLabel
import fr.twentynine.keepon.ui.component.KeepOnRichTooltip
import fr.twentynine.keepon.ui.component.LabeledControlRow
import fr.twentynine.keepon.ui.component.RoundedCheckbox
import fr.twentynine.keepon.ui.component.SwipeableScreenTimeoutCard
import fr.twentynine.keepon.ui.component.TimeoutIconChip
import fr.twentynine.keepon.ui.component.TipsSection
import kotlinx.coroutines.launch

/**
 * Home destination, stateful wrapper: bumps the app-launch count on first composition and forwards
 * the relevant slices of [uiState] to the stateless [HomeScreen].
 */
@Composable
fun HomeRoute(
    uiState: MainViewUIState.Success,
    onEvent: (MainUIEvent) -> Unit,
    navType: KeepOnNavigationType,
    paddingValue: PaddingValues,
) {
    LaunchedEffect(Unit) {
        onEvent(MainUIEvent.IncrementAppLaunchCount)
    }

    HomeScreen(
        tipsList = uiState.tipsList,
        resetTimeoutWhenScreenOff = uiState.resetTimeoutWhenScreenOff,
        screenTimeouts = uiState.screenTimeouts,
        timeoutIconStyle = uiState.timeoutIconStyle,
        showFirstLaunchHint = uiState.showFirstLaunchHint,
        onEvent = onEvent,
        navType = navType,
        paddingValue = paddingValue,
    )
}

/**
 * Home destination content (stateless): the tips section, the "reset on screen off" toggle, and the
 * scrollable list of selectable timeouts. Emits user actions as [MainUIEvent]s through [onEvent].
 */
@Composable
fun HomeScreen(
    tipsList: List<TipInfo>,
    resetTimeoutWhenScreenOff: Boolean,
    screenTimeouts: List<ScreenTimeoutUI>,
    timeoutIconStyle: TimeoutIconStyle,
    showFirstLaunchHint: Boolean,
    onEvent: (MainUIEvent) -> Unit,
    navType: KeepOnNavigationType,
    paddingValue: PaddingValues,
) {
    val baseMaxWidthModifier = screenContentModifier

    LazyColumn(
        modifier = Modifier
            .padding(paddingValue)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item(key = "tipsCard") {
            TipsSection(tipsList = tipsList, onEvent = onEvent, modifier = baseMaxWidthModifier.padding(top = 8.dp))
        }

        item(key = "behaviorCard") {
            KeepOnBehaviorCard(
                resetTimeoutWhenScreenOff = resetTimeoutWhenScreenOff,
                defaultScreenTimeoutUI = remember(screenTimeouts) {
                    screenTimeouts.firstOrNull { it.isDefault } ?: screenTimeouts.first()
                },
                onEvent = onEvent,
                modifier = baseMaxWidthModifier.fillMaxHeight(),
            )
        }

        item(key = "timeoutsCardHeader") {
            Column(
                modifier = baseMaxWidthModifier
                    .padding(top = 20.dp),
            ) {
                CardHeader(
                    iconVector = Icons.AutoMirrored.Rounded.PlaylistAddCheck,
                    iconSize = 22,
                    title = stringResource(R.string.select_timeouts_title),
                    descText = stringResource(R.string.select_timeouts_text),
                )
            }
        }

        itemsIndexed(
            items = screenTimeouts,
            key = { _, item -> "timeout_${item.value}" }
        ) { index, screenTimeout ->
            val itemPosition = remember(index, screenTimeouts.size) {
                ItemPosition.getItemPosition(index, screenTimeouts.size)
            }

            ScreenTimeoutRow(
                item = screenTimeout,
                itemPosition = itemPosition,
                timeoutIconStyle = timeoutIconStyle,
                resetTimeoutWhenScreenOff = resetTimeoutWhenScreenOff,
                showFirstLaunchHint = showFirstLaunchHint,
                onEvent = onEvent,
                modifier = baseMaxWidthModifier,
            )
        }

        item(key = "bottomSpacer") {
            val spacerBottomHeight = bottomSpacerHeight(navType)
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
        CardHeader(
            icon = painterResource(R.drawable.ic_keepon),
            title = stringResource(R.string.general_behavior_title),
            descText = stringResource(R.string.general_behavior_text),
        )
        Card(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
                .align(alignment = Alignment.Start),
            shape = KeepOnCardShape,
        ) {
            LabeledControlRow(
                onClick = {
                    val newValue = !resetTimeoutWhenScreenOff
                    onEvent(MainUIEvent.SetResetTimeoutWhenScreenOff(newValue))
                },
                verticalPadding = StyleTopSwitchRowVerticalPadding,
                leading = {
                    Switch(
                        checked = resetTimeoutWhenScreenOff,
                        onCheckedChange = null,
                        thumbContent = if (resetTimeoutWhenScreenOff) {
                            {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(SwitchDefaults.IconSize),
                                )
                            }
                        } else {
                            null
                        }
                    )
                },
                label = {
                    Column {
                        Text(
                            modifier = Modifier.padding(bottom = 4.dp),
                            text = stringResource(R.string.general_behavior_switch_title),
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.titleSmall,
                        )
                        Text(
                            text = rememberBehaviorSwitchLabel(defaultScreenTimeoutUI.displayName),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                },
            )
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
    showFirstLaunchHint: Boolean,
    onEvent: (MainUIEvent) -> Unit,
    modifier: Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    val tooltipState = rememberTooltipState()

    val swipeEnable = remember(item.isLocked, resetTimeoutWhenScreenOff) { !item.isLocked && resetTimeoutWhenScreenOff }

    val onClickAction = remember<(ScreenTimeoutUI) -> Unit>(item, tooltipState, onEvent) {
        {
                clickedItem ->
            if (clickedItem.isLocked || clickedItem.isDefault) {
                coroutineScope.launch { tooltipState.show() }
            } else {
                onEvent(MainUIEvent.ToggleScreenTimeoutSelection(clickedItem))
            }
        }
    }
    val onSwipeAction = remember<(Any, ScreenTimeoutUI) -> Unit>(onEvent) {
        {
                _, dismissedItem ->
            onEvent(
                MainUIEvent.SetDefaultScreenTimeout(
                    ScreenTimeoutUIToScreenTimeoutMapper.map(
                        dismissedItem
                    )
                )
            )
        }
    }

    val onFirstLaunchHintPlayed = remember(onEvent) {
        { onEvent(MainUIEvent.FirstLaunchHintPlayed) }
    }

    SwipeableScreenTimeoutCard(
        modifier = modifier
            .fillMaxSize(),
        item = item,
        itemPosition = itemPosition,
        swipeEnabled = swipeEnable,
        showFirstLaunchHint = showFirstLaunchHint,
        onFirstLaunchHintPlayed = onFirstLaunchHintPlayed,
        onClickAction = onClickAction,
        onSwipeAction = onSwipeAction,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            TimeoutIconChip(
                screenTimeout = ScreenTimeoutUIToScreenTimeoutMapper.map(item),
                timeoutIconStyle = timeoutIconStyle,
                contentDescription = item.displayName,
                modifier = Modifier.align(Alignment.CenterStart),
            )

            CurrentTimeoutDot(
                visible = item.isCurrent,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 29.dp, top = 1.dp),
            )

            Row(
                modifier = Modifier
                    .padding(start = 72.dp)
                    .align(Alignment.CenterStart),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TimeoutRowLabel(text = item.displayName)
                DefaultTimeoutBadge(
                    visible = item.isDefault && resetTimeoutWhenScreenOff,
                    modifier = Modifier.padding(start = 24.dp),
                )
            }

            when {
                item.isLocked -> {
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .align(Alignment.CenterEnd),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        KeepOnRichTooltip(
                            text = stringResource(R.string.timeout_locked_tooltips_text),
                            tooltipState = tooltipState,
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
                }
                item.isDefault -> {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        val tooltipTextId = remember(resetTimeoutWhenScreenOff) {
                            if (resetTimeoutWhenScreenOff) {
                                R.string.default_screen_timeout_tooltip_when_active
                            } else {
                                R.string.default_screen_timeout_tooltip_when_inactive
                            }
                        }
                        KeepOnRichTooltip(
                            text = stringResource(tooltipTextId),
                            tooltipState = tooltipState,
                        ) {
                            RoundedCheckbox(
                                checked = item.isSelected,
                                enabled = false,
                                onCheckedChange = null,
                                modifier = Modifier
                                    .padding(end = 8.dp)
                            )
                        }
                    }
                }
                else -> {
                    RoundedCheckbox(
                        checked = item.isSelected,
                        enabled = true,
                        onCheckedChange = null,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .align(Alignment.CenterEnd)
                    )
                }
            }
        }
    }
}

private val CurrentTimeoutDotSize = 8.dp

/** Shared fade/scale duration of the row markers (current dot, default badge) so they glide together. */
private const val ROW_MARKER_ANIMATION_MS = 450

/**
 * The accent dot marking the timeout currently configured on the system, shown just left of its row
 * label. Fades and scales in/out with [visible] so it glides between rows when the current timeout
 * changes (both the old and new rows are usually on screen, giving a cross-fade).
 */
@Composable
private fun CurrentTimeoutDot(visible: Boolean, modifier: Modifier = Modifier) {
    val progress by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(ROW_MARKER_ANIMATION_MS),
        label = "CurrentTimeoutDot",
    )
    if (progress > 0f) {
        Box(
            modifier = modifier
                .size(CurrentTimeoutDotSize)
                .graphicsLayer {
                    alpha = progress
                    scaleX = progress
                    scaleY = progress
                }
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.75f)),
        )
    }
}

private val DefaultTimeoutBadgeShape = RoundedCornerShape(50)

/** Constant dim of the whole badge (border + text) so it stays secondary next to the row label. */
private const val DEFAULT_TIMEOUT_BADGE_ALPHA = 0.8f

/**
 * The outlined pill marking the default timeout, shown after its row label. Fades and scales in/out
 * with [visible] so it glides between rows when the default timeout changes, like [CurrentTimeoutDot].
 */
@Composable
private fun DefaultTimeoutBadge(visible: Boolean, modifier: Modifier = Modifier) {
    val progress by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(ROW_MARKER_ANIMATION_MS),
        label = "DefaultTimeoutBadge",
    )
    if (progress > 0f) {
        Text(
            text = stringResource(R.string.default_timeout_badge_text),
            modifier = modifier
                .graphicsLayer {
                    alpha = progress * DEFAULT_TIMEOUT_BADGE_ALPHA
                    scaleX = progress
                    scaleY = progress
                }
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, DefaultTimeoutBadgeShape)
                .padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
    }
}
