package fr.twentynine.keepon.ui.view

import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import fr.twentynine.keepon.R
import fr.twentynine.keepon.data.model.MainUIEvent
import fr.twentynine.keepon.data.model.MainViewUIState
import fr.twentynine.keepon.data.model.NeededPermission
import fr.twentynine.keepon.data.model.TaskerEditUIState
import fr.twentynine.keepon.data.model.TaskerUIEvent

@Composable
fun MainPermissionScreen(
    uiState: MainViewUIState.Success,
    onEvent: (MainUIEvent) -> Unit,
) {
    val permissionScreenBatteryOptimizationTitle = stringResource(
        R.string.permissions_screen_battery_optimization_title
    )
    val permissionScreenBatteryOptimizationSubtitle = stringResource(
        R.string.permissions_screen_battery_optimization_subtitle
    )
    val permissionScreenWriteSettingTitle = stringResource(R.string.permissions_screen_write_setting_title)
    val permissionScreenWriteSettingSubtitle = stringResource(R.string.permissions_screen_write_setting_subtitle)
    val permissionScreenNotificationTitle = stringResource(R.string.permissions_screen_notification_title)
    val permissionScreenNotificationSubtitle = stringResource(R.string.permissions_screen_notification_subtitle)

    val neededPermissionList by remember(
        uiState.batteryIsNotOptimized,
        uiState.canWriteSystemSettings,
        uiState.canPostNotification
    ) {
        val mutableNeededPermissionList = mutableListOf(
            NeededPermission(
                title = permissionScreenBatteryOptimizationTitle,
                description = permissionScreenBatteryOptimizationSubtitle,
                requestNeeded = !uiState.batteryIsNotOptimized,
                requestAction = { onEvent(MainUIEvent.RequestDisableBatteryOptimization) },
            ),
            NeededPermission(
                title = permissionScreenWriteSettingTitle,
                description = permissionScreenWriteSettingSubtitle,
                requestNeeded = !uiState.canWriteSystemSettings,
                requestAction = { onEvent(MainUIEvent.RequestWriteSystemSettingPermission) },
            )
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            mutableNeededPermissionList.add(
                0,
                NeededPermission(
                    title = permissionScreenNotificationTitle,
                    description = permissionScreenNotificationSubtitle,
                    requestNeeded = !uiState.canPostNotification,
                    requestAction = { onEvent(MainUIEvent.RequestPostNotification) },
                ),
            )
        }
        derivedStateOf { mutableNeededPermissionList.toList() }
    }

    RequestPermissionsView(
        neededPermissionList = neededPermissionList,
        updateIsFirstLaunch = { onEvent(MainUIEvent.UpdateIsFirstLaunch) },
    )
}

@Composable
fun TaskerPermissionScreen(
    uiState: TaskerEditUIState.Success,
    onEvent: (TaskerUIEvent) -> Unit,
) {
    val permissionScreenBatteryOptimizationTitle = stringResource(
        R.string.permissions_screen_battery_optimization_title
    )
    val permissionScreenBatteryOptimizationSubtitle = stringResource(
        R.string.permissions_screen_battery_optimization_subtitle
    )
    val permissionScreenWriteSettingTitle = stringResource(R.string.permissions_screen_write_setting_title)
    val permissionScreenWriteSettingSubtitle = stringResource(R.string.permissions_screen_write_setting_subtitle)
    val permissionScreenNotificationTitle = stringResource(R.string.permissions_screen_notification_title)
    val permissionScreenNotificationSubtitle = stringResource(R.string.permissions_screen_notification_subtitle)

    val neededPermissionList by remember(
        uiState.batteryIsNotOptimized,
        uiState.canWriteSystemSettings,
        uiState.canPostNotification
    ) {
        val mutableNeededPermissionList = mutableListOf(
            NeededPermission(
                title = permissionScreenBatteryOptimizationTitle,
                description = permissionScreenBatteryOptimizationSubtitle,
                requestNeeded = !uiState.batteryIsNotOptimized,
                requestAction = { onEvent(TaskerUIEvent.RequestDisableBatteryOptimization) },
            ),
            NeededPermission(
                title = permissionScreenWriteSettingTitle,
                description = permissionScreenWriteSettingSubtitle,
                requestNeeded = !uiState.canWriteSystemSettings,
                requestAction = { onEvent(TaskerUIEvent.RequestWriteSystemSettingPermission) },
            )
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            mutableNeededPermissionList.add(
                0,
                NeededPermission(
                    title = permissionScreenNotificationTitle,
                    description = permissionScreenNotificationSubtitle,
                    requestNeeded = !uiState.canPostNotification,
                    requestAction = { onEvent(TaskerUIEvent.RequestPostNotification) },
                ),
            )
        }
        derivedStateOf { mutableNeededPermissionList.toList() }
    }

    RequestPermissionsView(
        neededPermissionList = neededPermissionList,
        updateIsFirstLaunch = { onEvent(TaskerUIEvent.UpdateIsFirstLaunch) },
    )
}

@Composable
fun RequestPermissionsView(
    neededPermissionList: List<NeededPermission>,
    updateIsFirstLaunch: () -> Unit,
) {
    val firstNeededPermissionIndex by remember(neededPermissionList) {
        derivedStateOf { neededPermissionList.indexOfFirst { it.requestNeeded } }
    }
    val lastNeededPermissionIndex by remember(neededPermissionList) {
        derivedStateOf { neededPermissionList.indexOfLast { it.requestNeeded } }
    }

    LaunchedEffect(key1 = firstNeededPermissionIndex) {
        if (firstNeededPermissionIndex == -1) {
            updateIsFirstLaunch()
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValue ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValue),
        ) {
            Image(
                imageVector = ImageVector.vectorResource(R.drawable.ic_perm_request),
                contentDescription = stringResource(R.string.permissions_screen_title),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 32.dp, vertical = 16.dp)
                    .align(Alignment.CenterHorizontally),
            )
            Column(
                modifier = Modifier
                    .wrapContentHeight()
                    .padding(bottom = 24.dp),
            ) {
                Text(
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 12.dp)
                        .align(Alignment.CenterHorizontally),
                    text = stringResource(R.string.permissions_screen_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                )
                Text(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
                    text = stringResource(R.string.permissions_screen_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                )

                neededPermissionList.forEachIndexed { index, neededPermission ->
                    PermissionItem(
                        permission = neededPermission,
                        index = index,
                        isActivated = index == firstNeededPermissionIndex,
                        onPermissionClick = {
                            neededPermission.requestAction()
                            if (index == lastNeededPermissionIndex) {
                                if (firstNeededPermissionIndex != -1) {
                                    updateIsFirstLaunch()
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PermissionItem(
    permission: NeededPermission,
    index: Int,
    isActivated: Boolean,
    onPermissionClick: () -> Unit,
) {
    val circleBackgroundColor by animateColorAsState(
        targetValue = if (isActivated) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            if (permission.requestNeeded) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            }
        },
        label = "circleBackgroundColorAnim"
    )

    val circleContentColor by animateColorAsState(
        targetValue = if (isActivated) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            if (permission.requestNeeded) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            }
        },
        label = "circleContentColorAnim"
    )

    val titleTextColor by animateColorAsState(
        targetValue = if (permission.requestNeeded) {
            MaterialTheme.colorScheme.onBackground
        } else {
            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        },
        label = "titleTextColorAnim"
    )

    val subtitleTextColor by animateColorAsState(
        targetValue = if (permission.requestNeeded) {
            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
        } else {
            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        },
        label = "subtitleTextColorAnim"
    )

    Row(
        modifier = Modifier
            .clickable(
                enabled = isActivated,
                onClick = onPermissionClick
            )
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .fillMaxWidth(),
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .padding(8.dp)
                .clip(CircleShape)
                .background(circleBackgroundColor)
                .align(Alignment.CenterVertically),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = (index + 1).toString(),
                style = MaterialTheme.typography.titleSmall,
                color = circleContentColor,
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
                .align(Alignment.CenterVertically),
            horizontalAlignment = Alignment.Start,
        ) {
            Text(
                modifier = Modifier.padding(bottom = 4.dp),
                text = permission.title,
                style = MaterialTheme.typography.labelLarge,
                color = titleTextColor,
                textAlign = TextAlign.Start,
            )
            Text(
                text = permission.description,
                style = MaterialTheme.typography.bodyMedium,
                color = subtitleTextColor,
                textAlign = TextAlign.Start,
            )
        }
        PermissionItemIndicator(
            isActivated = isActivated,
            requestNeeded = permission.requestNeeded
        )
    }
}

@Composable
fun PermissionItemIndicator(
    isActivated: Boolean,
    requestNeeded: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .wrapContentWidth()
            .padding(8.dp)
            .size(40.dp),
        contentAlignment = if (isActivated) Alignment.CenterStart else Alignment.Center,
    ) {
        if (isActivated) {
            val infiniteTransition = rememberInfiniteTransition(label = "arrowOffsetTransition")
            val density = LocalDensity.current
            val initialOffsetPx = with(density) { (-10).dp.toPx() }
            val targetOffsetPx = with(density) { 5.dp.toPx() }

            val offsetAnimationPx by infiniteTransition.animateValue(
                initialValue = initialOffsetPx,
                targetValue = targetOffsetPx,
                typeConverter = Float.VectorConverter,
                animationSpec = infiniteRepeatable(
                    animation = tween(1200),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "arrowOffset"
            )
            val offsetAnimationDp = with(density) { offsetAnimationPx.toDp() }

            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                contentDescription = stringResource(R.string.permission_screen_request_icon_desc),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(40.dp)
                    .offset(x = offsetAnimationDp),
            )
        } else {
            if (requestNeeded) {
                Spacer(modifier = Modifier.width(40.dp + 8.dp + 8.dp))
            } else {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = stringResource(R.string.permission_screen_check_icon_desc),
                    tint = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(30.dp),
                )
            }
        }
    }
}
