package fr.twentynine.keepon.ui.view

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import fr.twentynine.keepon.R
import fr.twentynine.keepon.data.enums.ItemPosition
import fr.twentynine.keepon.data.enums.SpecialScreenTimeoutType
import fr.twentynine.keepon.data.enums.TimeoutIconSize
import fr.twentynine.keepon.data.mapper.ScreenTimeoutUIToScreenTimeoutMapper
import fr.twentynine.keepon.data.model.ScreenTimeout
import fr.twentynine.keepon.data.model.ScreenTimeoutUI
import fr.twentynine.keepon.data.model.TaskerEditUIState
import fr.twentynine.keepon.data.model.TaskerUIEvent
import fr.twentynine.keepon.data.model.TimeoutIconData
import fr.twentynine.keepon.data.model.TimeoutIconStyle
import fr.twentynine.keepon.ui.util.GlowingText
import fr.twentynine.keepon.ui.util.MAX_SCREEN_CONTENT_WIDTH_IN_DP
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskerEditView(
    uiState: TaskerEditUIState,
    saveTaskerConfiguration: () -> Unit,
    onEvent: (TaskerUIEvent) -> Unit,
) {
    when {
        uiState is TaskerEditUIState.Error -> ErrorView(errorMessage = uiState.error)
        uiState is TaskerEditUIState.Success && (!uiState.canWriteSystemSettings || !uiState.batteryIsNotOptimized) -> TaskerPermissionScreen(
            uiState = uiState,
            onEvent = onEvent,
        )
        uiState is TaskerEditUIState.Success -> {
            val exitUntilCollapsedScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
            val scrollBehavior = remember { exitUntilCollapsedScrollBehavior }

            var scaffoldWidthDp by remember { mutableStateOf(0.dp) }
            val localDensity = LocalDensity.current
            val layoutDirection = LocalLayoutDirection.current

            val startPadding = getStartPaddingForDisplayCutout(scaffoldWidthDp, localDensity, layoutDirection)
            val endPadding = getEndPaddingForDisplayCutout(scaffoldWidthDp, localDensity, layoutDirection)
            val bottomPadding = getBottomPadding(localDensity)

            val backPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

            Scaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .onGloballyPositioned { coordinates ->
                        scaffoldWidthDp = with(localDensity) { coordinates.size.width.toDp() }
                    }
                    .padding(
                        start = startPadding,
                        end = endPadding,
                        bottom = bottomPadding,
                    ),
                containerColor = MaterialTheme.colorScheme.background,
                topBar = {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                text = stringResource(R.string.app_name),
                                style = MaterialTheme.typography.headlineLarge,
                            )
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background,
                            scrolledContainerColor = MaterialTheme.colorScheme.background,
                            navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                            titleContentColor = MaterialTheme.colorScheme.onBackground,
                            actionIconContentColor = MaterialTheme.colorScheme.onBackground
                        ),
                        navigationIcon = {
                            IconButton(onClick = { backPressedDispatcher?.onBackPressed() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                    contentDescription = null
                                )
                            }
                        },
                        scrollBehavior = scrollBehavior
                    )
                },
                floatingActionButton = {
                    if (uiState.selectedScreenTimeout != null) {
                        FloatingActionButton(
                            onClick = saveTaskerConfiguration,
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(68.dp),
                            shape = RoundedCornerShape(24.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Save,
                                contentDescription = stringResource(R.string.tasker_save_button),
                                modifier = Modifier.size(40.dp, 40.dp),
                            )
                        }
                    }
                }
            ) { paddingValue ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValue),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    TaskerEditScreen(
                        screenTimeouts = uiState.screenTimeouts,
                        specialScreenTimeouts = uiState.specialScreenTimeouts,
                        defaultScreenTimeout = uiState.defaultScreenTimeout,
                        previousScreenTimeout = uiState.previousScreenTimeout,
                        selectedScreenTimeout = uiState.selectedScreenTimeout,
                        timeoutIconStyle = uiState.timeoutIconStyle,
                        onEvent = onEvent,
                    )
                }
            }
        }
    }
}

@Composable
private fun getStartPaddingForDisplayCutout(
    boxWidthDp: Dp,
    density: androidx.compose.ui.unit.Density,
    layoutDirection: LayoutDirection
): Dp {
    return density.run {
        val safeDrawingInsets = WindowInsets.safeDrawing
        val startPadding = if (layoutDirection == LayoutDirection.Ltr) {
            safeDrawingInsets.getLeft(this, layoutDirection).toDp()
        } else {
            safeDrawingInsets.getRight(this, layoutDirection).toDp()
        }
        if (boxWidthDp <= MAX_SCREEN_CONTENT_WIDTH_IN_DP.dp + (startPadding * 2)) {
            startPadding
        } else {
            0.dp
        }
    }
}

@Composable
private fun getEndPaddingForDisplayCutout(
    boxWidthDp: Dp,
    density: androidx.compose.ui.unit.Density,
    layoutDirection: LayoutDirection
): Dp {
    return density.run {
        val safeDrawingInsets = WindowInsets.safeDrawing
        val endPadding = if (layoutDirection == LayoutDirection.Ltr) {
            safeDrawingInsets.getRight(this, layoutDirection).toDp()
        } else {
            safeDrawingInsets.getLeft(this, layoutDirection).toDp()
        }
        if (boxWidthDp <= MAX_SCREEN_CONTENT_WIDTH_IN_DP.dp + (endPadding * 2)) {
            endPadding
        } else {
            0.dp
        }
    }
}

@Composable
private fun getBottomPadding(density: androidx.compose.ui.unit.Density): Dp {
    return density.run { WindowInsets.displayCutout.getBottom(this).toDp() }
}

@Composable
fun TaskerEditScreen(
    screenTimeouts: List<ScreenTimeoutUI>,
    specialScreenTimeouts: List<ScreenTimeoutUI>,
    defaultScreenTimeout: ScreenTimeout,
    previousScreenTimeout: ScreenTimeout,
    selectedScreenTimeout: ScreenTimeoutUI?,
    timeoutIconStyle: TimeoutIconStyle,
    onEvent: (TaskerUIEvent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TaskerScreenTimeoutList(
            screenTimeouts = screenTimeouts,
            specialScreenTimeouts = specialScreenTimeouts,
            defaultScreenTimeout = defaultScreenTimeout,
            previousScreenTimeout = previousScreenTimeout,
            selectedScreenTimeout = selectedScreenTimeout,
            timeoutIconStyle = timeoutIconStyle,
            onEvent = onEvent,
        )
    }
}

@Composable
fun TaskerScreenTimeoutList(
    screenTimeouts: List<ScreenTimeoutUI>,
    specialScreenTimeouts: List<ScreenTimeoutUI>,
    defaultScreenTimeout: ScreenTimeout,
    previousScreenTimeout: ScreenTimeout,
    selectedScreenTimeout: ScreenTimeoutUI?,
    timeoutIconStyle: TimeoutIconStyle,
    onEvent: (TaskerUIEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val maxWidthModifier = remember {
        Modifier
            .fillMaxHeight()
            .width(MAX_SCREEN_CONTENT_WIDTH_IN_DP.dp)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item(key = "headerCard") {
            Column(
                modifier = maxWidthModifier
                    .padding(top = 28.dp)
            ) {
                CardHeaderView(
                    title = stringResource(R.string.select_tasker_timeouts_title),
                    iconVector = Icons.Rounded.Build,
                    descText = stringResource(R.string.select_tasker_timeouts_text),
                )
            }
        }

        item(key = "dynamicTitle") {
            Column(modifier = maxWidthModifier) {
                Text(
                    text = stringResource(R.string.tasker_dynamic_value_text),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                    modifier = Modifier
                        .padding(start = 32.dp, end = 32.dp, bottom = 8.dp),
                )
            }
        }

        itemsIndexed(
            items = specialScreenTimeouts,
            key = { _, item -> "special_${item.value}" }
        ) { index, screenTimeout ->
            val itemPosition = remember(index, specialScreenTimeouts.size) {
                ItemPosition.getItemPosition(index, specialScreenTimeouts.size)
            }

            TaskerScreenTimeoutRow(
                item = screenTimeout,
                itemPosition = itemPosition,
                defaultScreenTimeout = defaultScreenTimeout,
                previousScreenTimeout = previousScreenTimeout,
                selectedScreenTimeout = selectedScreenTimeout,
                timeoutIconStyle = timeoutIconStyle,
                onEvent = onEvent,
                modifier = maxWidthModifier
            )
        }

        item(key = "staticTitle") {
            Column(modifier = maxWidthModifier) {
                Text(
                    text = stringResource(R.string.tasker_static_value_text),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                    modifier = Modifier
                        .padding(start = 32.dp, end = 32.dp, top = 8.dp, bottom = 8.dp),
                )
            }
        }

        itemsIndexed(
            items = screenTimeouts,
            key = { _, item -> "static_${item.value}" }
        ) { index, screenTimeout ->
            val itemPosition = remember(index, screenTimeouts.size) {
                ItemPosition.getItemPosition(index, screenTimeouts.size)
            }

            TaskerScreenTimeoutRow(
                item = screenTimeout,
                itemPosition = itemPosition,
                defaultScreenTimeout = defaultScreenTimeout,
                previousScreenTimeout = previousScreenTimeout,
                selectedScreenTimeout = selectedScreenTimeout,
                timeoutIconStyle = timeoutIconStyle,
                onEvent = onEvent,
                modifier = maxWidthModifier
            )
        }

        item(key = "bottomSpacer") {
            if (selectedScreenTimeout != null) {
                Spacer(modifier = Modifier.padding(bottom = 96.dp))
            } else {
                Spacer(modifier = Modifier.padding(bottom = 18.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskerScreenTimeoutRow(
    item: ScreenTimeoutUI,
    itemPosition: ItemPosition,
    defaultScreenTimeout: ScreenTimeout,
    previousScreenTimeout: ScreenTimeout,
    selectedScreenTimeout: ScreenTimeoutUI?,
    timeoutIconStyle: TimeoutIconStyle,
    onEvent: (TaskerUIEvent) -> Unit,
    modifier: Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val tooltipState = rememberTooltipState()

    val isSelected = remember(item, selectedScreenTimeout) { item.value == selectedScreenTimeout?.value }

    ItemCardView(
        itemPosition = itemPosition,
        modifier = modifier
    ) {
        val clickLambda: () -> Unit = remember(item, tooltipState, onEvent) {
            {
                if (item.isLocked) {
                    coroutineScope.launch { tooltipState.show() }
                } else {
                    onEvent(TaskerUIEvent.SetSelectedScreenTimeout(item))
                }
            }
        }

        Row(
            modifier = Modifier
                .clickable(onClick = clickLambda)
                .padding(12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val roundedCornerShape = remember { RoundedCornerShape(14.dp) }
            val backgroundColorAlpha = 0.65f
            val borderColorAlpha = 0.35f

            val itemValue = remember(item, defaultScreenTimeout, previousScreenTimeout) {
                when (item.value) {
                    SpecialScreenTimeoutType.DEFAULT_SCREEN_TIMEOUT_TYPE.value -> defaultScreenTimeout
                    SpecialScreenTimeoutType.PREVIOUS_SCREEN_TIMEOUT_TYPE.value -> previousScreenTimeout
                    else -> ScreenTimeoutUIToScreenTimeoutMapper.map(item)
                }
            }

            val imageData = remember(timeoutIconStyle) {
                TimeoutIconData(
                    itemValue,
                    TimeoutIconSize.MEDIUM,
                    timeoutIconStyle
                )
            }

            Box(
                modifier = Modifier
                    .clip(roundedCornerShape)
                    .background(
                        MaterialTheme.colorScheme.surface.copy(
                            alpha = backgroundColorAlpha
                        )
                    )
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(
                            alpha = borderColorAlpha
                        ),
                        shape = roundedCornerShape
                    )
                    .size(38.dp),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    modifier = Modifier.size(20.dp, 20.dp),
                    model = imageData,
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant),
                    contentDescription = item.displayName,
                )
            }

            GlowingText(
                modifier = Modifier.padding(start = 24.dp),
                style = MaterialTheme.typography.labelMedium,
                text = item.displayName,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                showGlow = isSelected,
                glowColor = MaterialTheme.colorScheme.onSurface,
                glowRadius = 10.dp,
                glowSpread = 2.dp,
            )

            Spacer(Modifier.weight(1f))

            if (item.isLocked) {
                TooltipBox(
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
                    val iconClickLambda: () -> Unit = remember(tooltipState) {
                        {
                            coroutineScope.launch { tooltipState.show() }
                        }
                    }
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = stringResource(R.string.timeout_locked_icon_desc),
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .size(20.dp, 20.dp)
                            .clickable(onClick = iconClickLambda),
                    )
                }
            } else {
                RadioButton(
                    modifier = Modifier.padding(end = 4.dp),
                    selected = isSelected,
                    onClick = null,
                )
            }
        }
    }
}
