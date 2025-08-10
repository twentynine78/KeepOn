package fr.twentynine.keepon.ui.view

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.size.Size
import fr.twentynine.keepon.R
import fr.twentynine.keepon.data.enums.TimeoutIconSize
import fr.twentynine.keepon.data.model.MainUIEvent
import fr.twentynine.keepon.data.model.MainViewUIState
import fr.twentynine.keepon.data.model.TimeoutIconData
import fr.twentynine.keepon.ui.navigation.KeepOnNavigationWrapper
import fr.twentynine.keepon.ui.navigation.NavigationActions
import fr.twentynine.keepon.ui.navigation.NavigationDestination
import fr.twentynine.keepon.ui.navigation.NavigationDestinationWithBadge
import fr.twentynine.keepon.ui.navigation.TOP_LEVEL_DESTINATIONS
import fr.twentynine.keepon.ui.navigation.withBadge
import fr.twentynine.keepon.ui.util.KeepOnNavigationType
import fr.twentynine.keepon.ui.util.MAX_SCREEN_CONTENT_WIDTH_IN_DP

private fun NavigationSuiteType.toKeepOnNavType() = when (this) {
    NavigationSuiteType.NavigationBar -> KeepOnNavigationType.BOTTOM_NAVIGATION
    NavigationSuiteType.NavigationRail -> KeepOnNavigationType.NAVIGATION_RAIL
    else -> KeepOnNavigationType.BOTTOM_NAVIGATION
}

private val enterTransitionSpec = tween<Float>(300)
private val exitTransitionSpec = tween<Float>(300)

@Composable
fun MainView(
    uiState: MainViewUIState,
    onEvent: (MainUIEvent) -> Unit,
) {
    when {
        uiState is MainViewUIState.Error -> ErrorView(errorMessage = uiState.error)
        uiState is MainViewUIState.Success && (uiState.isFirstLaunch || !uiState.canWriteSystemSettings || !uiState.batteryIsNotOptimized) -> {
            MainPermissionScreen(
                uiState = uiState,
                onEvent = onEvent,
            )
        }
        uiState is MainViewUIState.Success -> {
            KeepOnView(
                uiState = uiState,
                onEvent = onEvent,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun KeepOnView(
    uiState: MainViewUIState.Success,
    onEvent: (MainUIEvent) -> Unit,
) {
    val navController = rememberNavController()
    val navigationActions = remember(navController) {
        NavigationActions(navController)
    }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val selectedDestination = navBackStackEntry?.destination?.route ?: NavigationDestination.Home.route

    val topLevelDestinations = remember(uiState.tipsList) {
        TOP_LEVEL_DESTINATIONS.map { destination ->
            when (destination) {
                NavigationDestination.Home -> destination.withBadge(uiState.tipsList.size)
                else -> NavigationDestinationWithBadge(
                    destination
                )
            }
        }
    }

    val topBarScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val bottomBarScrollBehavior = BottomAppBarDefaults.exitAlwaysScrollBehavior()

    val colorScheme = MaterialTheme.colorScheme
    val primaryContainerColor = colorScheme.primaryContainer
    val backgroundColor = colorScheme.background
    val onBackgroundColor = colorScheme.onBackground
    val onPrimaryContainerColor = colorScheme.onPrimaryContainer

    val topAppBarColors = remember(backgroundColor, onBackgroundColor) {
        TopAppBarColors(
            containerColor = backgroundColor,
            scrolledContainerColor = backgroundColor,
            navigationIconContentColor = onBackgroundColor,
            titleContentColor = onBackgroundColor,
            actionIconContentColor = onBackgroundColor
        )
    }

    KeepOnNavigationWrapper(
        topLevelDestinations = topLevelDestinations,
        selectedDestination = selectedDestination,
        keepOnIsActive = uiState.keepOnIsActive,
        currentScreenTimeout = uiState.currentScreenTimeout,
        screenTimeouts = uiState.screenTimeouts,
        timeoutIconStyle = uiState.timeoutIconStyle,
        fabOnClick = { onEvent(MainUIEvent.SetNextSelectedSystemScreenTimeout) },
        scrollBehavior = bottomBarScrollBehavior,
        navigateToTopLevelDestination = navigationActions::navigateTo,
    ) {
        val navType = navSuiteType.toKeepOnNavType()

        val localDensity = LocalDensity.current
        var scaffoldWidthDp by remember { mutableStateOf(0.dp) }

        val scaffoldModifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { coordinates ->
                val newWidthDp = with(localDensity) { coordinates.size.width.toDp() }
                if (scaffoldWidthDp != newWidthDp) {
                    scaffoldWidthDp = newWidthDp
                }
            }
            .padding(
                start = getStartPaddingForDisplayCutout(scaffoldWidthDp, navType),
                end = getEndPaddingForDisplayCutout(scaffoldWidthDp),
                bottom = getBottomPadding(navType),
            )
            .nestedScroll(topBarScrollBehavior.nestedScrollConnection)
            .nestedScroll(bottomBarScrollBehavior.nestedScrollConnection)

        Scaffold(
            modifier = scaffoldModifier,
            contentWindowInsets = WindowInsets.safeDrawing,
            containerColor = backgroundColor,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.headlineLarge,
                        )
                    },
                    colors = topAppBarColors,
                    scrollBehavior = topBarScrollBehavior
                )
            },
            floatingActionButton = {
                if (navType == KeepOnNavigationType.BOTTOM_NAVIGATION) {
                    val animationDuration = 50

                    val fabBackgroundColor by animateColorAsState(
                        targetValue = if (uiState.keepOnIsActive) primaryContainerColor else backgroundColor,
                        animationSpec = tween(animationDuration),
                        label = "fabBackgroundColor"
                    )
                    val fabBorderColor by animateColorAsState(
                        targetValue = if (uiState.keepOnIsActive) backgroundColor else primaryContainerColor,
                        animationSpec = tween(animationDuration),
                        label = "fabBorderColor"
                    )
                    val fabContentColor by animateColorAsState(
                        targetValue = if (uiState.keepOnIsActive) onPrimaryContainerColor else onBackgroundColor,
                        animationSpec = tween(animationDuration),
                        label = "fabContentColor"
                    )

                    val fabContentDescription = remember(uiState.currentScreenTimeout, uiState.screenTimeouts) {
                        uiState.screenTimeouts.firstOrNull { it.value == uiState.currentScreenTimeout.value }?.displayName
                            ?: "Timeout Icon"
                    }

                    FloatingActionButton(
                        modifier = Modifier
                            .border(
                                width = 1.dp,
                                color = fabBorderColor,
                                RoundedCornerShape(24.dp)
                            )
                            .size(68.dp),
                        onClick = { onEvent(MainUIEvent.SetNextSelectedSystemScreenTimeout) },
                        containerColor = fabBackgroundColor,
                        contentColor = fabContentColor,
                        shape = RoundedCornerShape(24.dp),
                    ) {
                        val timeoutIconData = remember(uiState.currentScreenTimeout, uiState.timeoutIconStyle) {
                            TimeoutIconData(
                                uiState.currentScreenTimeout,
                                TimeoutIconSize.LARGE,
                                uiState.timeoutIconStyle
                            )
                        }
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(timeoutIconData)
                                .size(
                                    Size.ORIGINAL
                                )
                                .build(),
                            contentDescription = fabContentDescription,
                            colorFilter = ColorFilter.tint(fabContentColor),
                            modifier = Modifier.size(40.dp, 40.dp).padding(bottom = 2.dp),
                        )
                    }
                }
            },
        ) { paddingValue ->
            val screenPaddingModifier = remember(navType, paddingValue) {
                when (navType) {
                    KeepOnNavigationType.BOTTOM_NAVIGATION -> Modifier.padding(
                        top = paddingValue.calculateTopPadding()
                    )
                    KeepOnNavigationType.NAVIGATION_RAIL -> Modifier.padding(
                        top = paddingValue.calculateTopPadding(),
                        bottom = paddingValue.calculateBottomPadding()
                    )
                }
            }

            Box(
                modifier = screenPaddingModifier
                    .fillMaxSize(),
                contentAlignment = Alignment.TopCenter,
            ) {
                KeepOnNavHost(
                    navController = navController,
                    uiState = uiState,
                    onEvent = onEvent,
                    navType = navType,
                )
            }
        }
    }
}

@Composable
private fun getStartPaddingForDisplayCutout(
    boxWidthDp: Dp,
    navType: KeepOnNavigationType,
): Dp {
    return LocalDensity.current.run {
        val safeDrawingInsets = WindowInsets.safeDrawing
        val startPadding = if (LocalLayoutDirection.current == LayoutDirection.Ltr) {
            safeDrawingInsets.getLeft(this, LocalLayoutDirection.current).toDp()
        } else {
            safeDrawingInsets.getRight(this, LocalLayoutDirection.current).toDp()
        }
        if (navType == KeepOnNavigationType.BOTTOM_NAVIGATION && boxWidthDp <= MAX_SCREEN_CONTENT_WIDTH_IN_DP.dp + (startPadding * 2)) {
            startPadding
        } else {
            0.dp
        }
    }
}

@Composable
private fun getEndPaddingForDisplayCutout(
    boxWidthDp: Dp,
): Dp {
    return LocalDensity.current.run {
        val safeDrawingInsets = WindowInsets.safeDrawing
        val endPadding = if (LocalLayoutDirection.current == LayoutDirection.Ltr) {
            safeDrawingInsets.getRight(this, LocalLayoutDirection.current).toDp()
        } else {
            safeDrawingInsets.getLeft(this, LocalLayoutDirection.current).toDp()
        }
        if (boxWidthDp <= MAX_SCREEN_CONTENT_WIDTH_IN_DP.dp + (endPadding * 2)) {
            endPadding
        } else {
            0.dp
        }
    }
}

@Composable
private fun getBottomPadding(
    navType: KeepOnNavigationType
): Dp {
    return LocalDensity.current.run {
        if (navType == KeepOnNavigationType.BOTTOM_NAVIGATION) {
            0.dp
        } else {
            WindowInsets.displayCutout.getBottom(this).toDp()
        }
    }
}

@Composable
private fun KeepOnNavHost(
    navController: NavHostController,
    uiState: MainViewUIState.Success,
    onEvent: (MainUIEvent) -> Unit,
    navType: KeepOnNavigationType,
) {
    NavHost(
        modifier = Modifier
            .fillMaxSize(),
        navController = navController,
        startDestination = NavigationDestination.Home.route,
    ) {
        composable(
            route = NavigationDestination.Home.route,
            enterTransition = { fadeIn(animationSpec = enterTransitionSpec) },
            exitTransition = { fadeOut(animationSpec = exitTransitionSpec) },
        ) {
            HomeView(
                uiState = uiState,
                onEvent = onEvent,
                navType = navType,
            )
        }
        composable(
            route = NavigationDestination.Style.route,
            enterTransition = { fadeIn(animationSpec = enterTransitionSpec) },
            exitTransition = { fadeOut(animationSpec = exitTransitionSpec) },
        ) {
            StyleView(
                uiState = uiState,
                onEvent = onEvent,
                navType = navType,
            )
        }
        composable(
            route = NavigationDestination.About.route,
            enterTransition = { fadeIn(animationSpec = enterTransitionSpec) },
            exitTransition = { fadeOut(animationSpec = exitTransitionSpec) },
        ) {
            AboutView(navType = navType)
        }
    }
}
