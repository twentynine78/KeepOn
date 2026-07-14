package fr.twentynine.keepon.ui.screen

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.captionBar
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.MobileOff
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import fr.twentynine.keepon.R
import fr.twentynine.keepon.domain.model.TaskerEventType
import fr.twentynine.keepon.ui.component.CardHeader
import fr.twentynine.keepon.ui.component.ItemCard
import fr.twentynine.keepon.ui.component.TimeoutRowLabel
import fr.twentynine.keepon.ui.event.TaskerEventUIEvent
import fr.twentynine.keepon.ui.model.ItemPosition
import fr.twentynine.keepon.ui.model.TaskerEventUI
import fr.twentynine.keepon.ui.state.TaskerEventEditUIState
import fr.twentynine.keepon.ui.theme.KeepOnCardShape
import fr.twentynine.keepon.ui.util.FabBottomClearance
import fr.twentynine.keepon.ui.util.screenContentModifier

/**
 * Root of the Tasker plug-in event-edit activity. Shows the error/permission states when needed,
 * otherwise the [TaskerEventEditScreen] in a scaffold with a save affordance
 * ([saveTaskerConfiguration] finishes the activity, returning the chosen event to Tasker).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskerEventEditRoute(
    uiState: TaskerEventEditUIState,
    saveTaskerConfiguration: () -> Unit,
    onEvent: (TaskerEventUIEvent) -> Unit,
) {
    when (uiState) {
        is TaskerEventEditUIState.Error -> ErrorScreen(errorMessage = uiState.error)
        is TaskerEventEditUIState.Success -> {
            if (!uiState.canWriteSystemSettings || !uiState.batteryIsNotOptimized) {
                TaskerEventPermissionScreen(
                    uiState = uiState,
                    onEvent = onEvent,
                )
            } else {
                val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

                val combinedInsets = WindowInsets.safeDrawing.union(WindowInsets.captionBar)

                val backPressedDispatcher =
                    LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(scrollBehavior.nestedScrollConnection),
                    contentWindowInsets = combinedInsets,
                    containerColor = MaterialTheme.colorScheme.background,
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Text(
                                    text = stringResource(R.string.app_name),
                                    style = MaterialTheme.typography.headlineLarge,
                                )
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
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
                        if (uiState.selectedEvent != null) {
                            FloatingActionButton(
                                onClick = saveTaskerConfiguration,
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(68.dp),
                                shape = KeepOnCardShape,
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
                            .fillMaxSize(),
                        contentAlignment = Alignment.TopCenter,
                    ) {
                        TaskerEventEditScreen(
                            events = uiState.events,
                            selectedEvent = uiState.selectedEvent,
                            onEvent = onEvent,
                            paddingValue = paddingValue,
                        )
                    }
                }
            }
        }
        is TaskerEventEditUIState.Loading -> {}
    }
}

/**
 * Tasker event-edit content (stateless): the list of KeepOn plug-in events with the current
 * selection highlighted. Picking one emits a [TaskerEventUIEvent] through [onEvent].
 */
@Composable
fun TaskerEventEditScreen(
    events: List<TaskerEventUI>,
    selectedEvent: TaskerEventUI?,
    onEvent: (TaskerEventUIEvent) -> Unit,
    paddingValue: PaddingValues,
) {
    TaskerEventList(
        events = events,
        selectedEvent = selectedEvent,
        onEvent = onEvent,
        paddingValue = paddingValue,
    )
}

@Composable
fun TaskerEventList(
    events: List<TaskerEventUI>,
    selectedEvent: TaskerEventUI?,
    onEvent: (TaskerEventUIEvent) -> Unit,
    modifier: Modifier = Modifier,
    paddingValue: PaddingValues,
) {
    val maxWidthModifier = screenContentModifier

    LazyColumn(
        modifier = modifier
            .padding(paddingValue)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item(key = "headerCard") {
            Column(
                modifier = maxWidthModifier
                    .padding(top = 28.dp)
            ) {
                CardHeader(
                    title = stringResource(R.string.select_tasker_event_title),
                    iconVector = Icons.Rounded.Bolt,
                    descText = stringResource(R.string.select_tasker_event_text),
                )
            }
        }

        itemsIndexed(
            items = events,
            key = { _, item -> "event_${item.type.id}" }
        ) { index, event ->
            val itemPosition = remember(index, events.size) {
                ItemPosition.getItemPosition(index, events.size)
            }

            TaskerEventRow(
                item = event,
                itemPosition = itemPosition,
                isSelected = event.type == selectedEvent?.type,
                onEvent = onEvent,
                modifier = maxWidthModifier
            )
        }

        item(key = "bottomSpacer") {
            // The list's paddingValue already carries the system-navigation-bar inset, so the spacer
            // only needs the shared FAB clearance on top; with no selection there is no FAB, so a
            // small breathing gap is enough.
            if (selectedEvent != null) {
                Spacer(modifier = Modifier.padding(bottom = FabBottomClearance))
            } else {
                Spacer(modifier = Modifier.padding(bottom = 18.dp))
            }
        }
    }
}

@Composable
fun TaskerEventRow(
    item: TaskerEventUI,
    itemPosition: ItemPosition,
    isSelected: Boolean,
    onEvent: (TaskerEventUIEvent) -> Unit,
    modifier: Modifier
) {
    ItemCard(
        itemPosition = itemPosition,
        modifier = modifier
    ) {
        val clickLambda: () -> Unit = remember(item, onEvent) {
            {
                onEvent(TaskerEventUIEvent.SetSelectedEvent(item))
            }
        }

        Row(
            modifier = Modifier
                .clickable(onClick = clickLambda)
                .padding(14.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = eventIcon(item.type),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
            )

            TimeoutRowLabel(
                text = item.displayName,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 24.dp),
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
            )

            RadioButton(
                modifier = Modifier.padding(end = 4.dp),
                selected = isSelected,
                onClick = null,
            )
        }
    }
}

/** The glyph shown next to each plug-in event; extend the mapping alongside [TaskerEventType]. */
private fun eventIcon(type: TaskerEventType): ImageVector = when (type) {
    TaskerEventType.RESET_ON_SCREEN_OFF -> Icons.Rounded.MobileOff
}
