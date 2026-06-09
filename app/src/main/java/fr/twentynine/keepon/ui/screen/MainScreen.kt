package fr.twentynine.keepon.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.captionBar
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.union
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.material3.rememberBottomAppBarState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import fr.twentynine.keepon.R
import fr.twentynine.keepon.domain.model.ScreenTimeout
import fr.twentynine.keepon.ui.catalog.TipsInfo
import fr.twentynine.keepon.ui.component.PrefetchTimeoutIcons
import fr.twentynine.keepon.ui.component.TimeoutFab
import fr.twentynine.keepon.ui.event.MainUIEvent
import fr.twentynine.keepon.ui.state.MainViewUIState
import fr.twentynine.keepon.ui.navigation.KeepOnNavigationWrapper
import fr.twentynine.keepon.ui.navigation.NavigationActions
import fr.twentynine.keepon.ui.navigation.NavigationDestination
import fr.twentynine.keepon.ui.navigation.NavigationDestinationWithBadge
import fr.twentynine.keepon.ui.navigation.TOP_LEVEL_DESTINATIONS
import fr.twentynine.keepon.ui.navigation.withBadge
import fr.twentynine.keepon.ui.util.KeepOnNavigationType
import fr.twentynine.keepon.ui.util.plus

private fun NavigationSuiteType.toKeepOnNavType() = when (this) {
    NavigationSuiteType.NavigationBar -> KeepOnNavigationType.BOTTOM_NAVIGATION
    NavigationSuiteType.NavigationRail -> KeepOnNavigationType.NAVIGATION_RAIL
    else -> KeepOnNavigationType.BOTTOM_NAVIGATION
}

private const val SLIDE_ANIMATION_DURATION_MS = 300

private val enterPageTransitionSpec = tween<Float>(SLIDE_ANIMATION_DURATION_MS)
private val exitPageTransitionSpec = tween<Float>(SLIDE_ANIMATION_DURATION_MS)

private val defaultEnterTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition) =
    { fadeIn(animationSpec = enterPageTransitionSpec) }

private val defaultExitTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition) =
    { fadeOut(animationSpec = exitPageTransitionSpec) }

private val slideInFromRight = slideInHorizontally(
    initialOffsetX = { fullWidth -> fullWidth },
    animationSpec = tween(SLIDE_ANIMATION_DURATION_MS)
)
private val slideOutToLeft = slideOutHorizontally(
    targetOffsetX = { fullWidth -> -fullWidth },
    animationSpec = tween(SLIDE_ANIMATION_DURATION_MS)
)
private val slideInFromLeft = slideInHorizontally(
    initialOffsetX = { fullWidth -> -fullWidth },
    animationSpec = tween(SLIDE_ANIMATION_DURATION_MS)
)
private val slideOutToRight = slideOutHorizontally(
    targetOffsetX = { fullWidth -> fullWidth },
    animationSpec = tween(SLIDE_ANIMATION_DURATION_MS)
)
private val defaultFade = fadeIn(
    animationSpec = tween(SLIDE_ANIMATION_DURATION_MS)
).togetherWith(fadeOut(animationSpec = tween(SLIDE_ANIMATION_DURATION_MS)))

private enum class MainScreenState {
    EMPTY,
    ERROR,
    PERMISSION,
    KEEP_ON
}

@Composable
fun MainScreen(
    uiState: MainViewUIState,
    onEvent: (MainUIEvent) -> Unit,
) {
    val targetScreenState = remember(uiState) {
        when (uiState) {
            is MainViewUIState.Error -> MainScreenState.ERROR
            is MainViewUIState.Success -> {
                if (!uiState.canWriteSystemSettings || !uiState.batteryIsNotOptimized) {
                    MainScreenState.PERMISSION
                } else {
                    MainScreenState.KEEP_ON
                }
            }
            is MainViewUIState.Loading -> MainScreenState.EMPTY
        }
    }

    AnimatedContent(
        modifier = Modifier.fillMaxSize(),
        targetState = targetScreenState,
        transitionSpec = {
            when (targetState) {
                MainScreenState.KEEP_ON if initialState == MainScreenState.PERMISSION ->
                    slideInFromRight togetherWith slideOutToLeft

                MainScreenState.PERMISSION if initialState == MainScreenState.KEEP_ON ->
                    slideInFromLeft togetherWith slideOutToRight

                else -> defaultFade
            }
        },
        contentKey = { targetScreenState -> targetScreenState.toString() },
        label = "MainScreenAnimation"
    ) { screenStateTarget ->
        when (screenStateTarget) {
            MainScreenState.ERROR -> {
                if (uiState is MainViewUIState.Error) {
                    ErrorScreen(errorMessage = uiState.error)
                }
            }
            MainScreenState.PERMISSION -> {
                if (uiState is MainViewUIState.Success) {
                    MainPermissionScreen(
                        uiState = uiState,
                        onEvent = onEvent,
                    )
                }
            }
            MainScreenState.KEEP_ON -> {
                if (uiState is MainViewUIState.Success) {
                    KeepOnView(
                        uiState = uiState,
                        onEvent = onEvent,
                    )
                }
            }
            MainScreenState.EMPTY -> {}
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

    val topLevelDestinations = rememberTopLevelDestinations(uiState.tipsList)

    val topAppBarState = rememberTopAppBarState()
    val bottomAppBarScrollState = rememberBottomAppBarState()

    val topBarScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(state = topAppBarState)
    val bottomBarScrollBehavior = BottomAppBarDefaults.exitAlwaysScrollBehavior(
        state = bottomAppBarScrollState
    )
    val combinedNestedScrollConnection = remember(topBarScrollBehavior, bottomBarScrollBehavior) {
        topBarScrollBehavior.nestedScrollConnection + bottomBarScrollBehavior.nestedScrollConnection
    }

    val backgroundColor = MaterialTheme.colorScheme.background
    val onBackgroundColor = MaterialTheme.colorScheme.onBackground

    val fabOnClick = remember(onEvent) {
        {
            onEvent(MainUIEvent.SetNextSelectedSystemScreenTimeout)
        }
    }

    // Warm the LARGE icon cache for the timeouts the FAB cycles through, so a tap animates without
    // waiting for the incoming icon to be generated (the list chips only cache the MEDIUM size).
    val cycleTimeouts = remember(uiState.screenTimeouts) {
        uiState.screenTimeouts.filter { it.isSelected }.map { ScreenTimeout(it.value) }
    }
    PrefetchTimeoutIcons(cycleTimeouts, uiState.timeoutIconStyle)

    // The timeout the FAB would cycle to next, used as the interposed glyph in the icon-animation
    // preview (current -> next -> current). Derived from the selected list, so it is already prefetched.
    val previewNextTimeout = remember(cycleTimeouts, uiState.currentScreenTimeout) {
        val idx = cycleTimeouts.indexOf(uiState.currentScreenTimeout)
        when {
            cycleTimeouts.isEmpty() -> uiState.currentScreenTimeout
            idx == -1 -> cycleTimeouts.first()
            else -> cycleTimeouts[(idx + 1) % cycleTimeouts.size]
        }
    }

    KeepOnNavigationWrapper(
        topLevelDestinations = topLevelDestinations,
        selectedDestination = selectedDestination,
        keepOnIsActive = uiState.keepOnIsActive,
        currentScreenTimeout = uiState.currentScreenTimeout,
        nextScreenTimeout = previewNextTimeout,
        currentTimeoutDisplay = uiState.currentTimeoutDisplay,
        timeoutIconStyle = uiState.timeoutIconStyle,
        iconTransitionAnimation = uiState.iconTransitionAnimation,
        fabOnClick = fabOnClick,
        scrollBehavior = bottomBarScrollBehavior,
        navigateToTopLevelDestination = navigationActions::navigateTo,
    ) {
        val navType = navSuiteType.toKeepOnNavType()

        val combinedInsets = WindowInsets.safeDrawing.union(WindowInsets.captionBar)

        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(combinedNestedScrollConnection),
            contentWindowInsets = combinedInsets,
            containerColor = backgroundColor,
            topBar = {
                KeepOnTopAppBar(
                    scrollBehavior = topBarScrollBehavior,
                    backgroundColor = backgroundColor,
                    onBackgroundColor = onBackgroundColor
                )
            },
            floatingActionButton = {
                if (navType == KeepOnNavigationType.BOTTOM_NAVIGATION) {
                    TimeoutFab(
                        keepOnIsActive = uiState.keepOnIsActive,
                        currentScreenTimeout = uiState.currentScreenTimeout,
                        nextScreenTimeout = previewNextTimeout,
                        currentTimeoutDisplay = uiState.currentTimeoutDisplay,
                        timeoutIconStyle = uiState.timeoutIconStyle,
                        iconTransitionAnimation = uiState.iconTransitionAnimation,
                        onClick = fabOnClick,
                    )
                }
            },
        ) { scaffoldPaddingValue ->
            val layoutDirection = LocalLayoutDirection.current
            val bottomPadding = remember(navType, scaffoldPaddingValue) {
                if (navType == KeepOnNavigationType.BOTTOM_NAVIGATION) {
                    0.dp
                } else {
                    scaffoldPaddingValue.calculateBottomPadding()
                }
            }
            val startPadding = remember(navType, scaffoldPaddingValue) {
                if (navType == KeepOnNavigationType.NAVIGATION_RAIL) {
                    0.dp
                } else {
                    scaffoldPaddingValue.calculateStartPadding(layoutDirection)
                }
            }
            val paddingValue = PaddingValues(
                top = scaffoldPaddingValue.calculateTopPadding(),
                start = startPadding,
                end = scaffoldPaddingValue.calculateEndPadding(layoutDirection),
                bottom = bottomPadding
            )

            KeepOnContent(
                paddingValue = paddingValue,
                navController = navController,
                uiState = uiState,
                onEvent = onEvent,
                navType = navType
            )
        }
    }
}

@Composable
private fun rememberTopLevelDestinations(tipsList: List<TipsInfo>): List<NavigationDestinationWithBadge> {
    return remember(tipsList) {
        TOP_LEVEL_DESTINATIONS.map { destination ->
            when (destination) {
                NavigationDestination.Home -> destination.withBadge(tipsList.size)
                else -> NavigationDestinationWithBadge(destination)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun KeepOnTopAppBar(
    scrollBehavior: TopAppBarScrollBehavior,
    backgroundColor: Color,
    onBackgroundColor: Color
) {
    val topAppBarColors = remember(backgroundColor, onBackgroundColor) {
        TopAppBarColors(
            containerColor = backgroundColor,
            scrolledContainerColor = backgroundColor,
            navigationIconContentColor = onBackgroundColor,
            titleContentColor = onBackgroundColor,
            actionIconContentColor = onBackgroundColor,
            subtitleContentColor = onBackgroundColor,
        )
    }
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineLarge,
            )
        },

        colors = topAppBarColors,
        scrollBehavior = scrollBehavior
    )
}

@Composable
private fun KeepOnContent(
    paddingValue: PaddingValues,
    navController: NavHostController,
    uiState: MainViewUIState.Success,
    onEvent: (MainUIEvent) -> Unit,
    navType: KeepOnNavigationType
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        KeepOnNavHost(
            navController = navController,
            uiState = uiState,
            onEvent = onEvent,
            navType = navType,
            paddingValue = paddingValue,
        )
    }
}

@Composable
private fun KeepOnNavHost(
    navController: NavHostController,
    uiState: MainViewUIState.Success,
    onEvent: (MainUIEvent) -> Unit,
    navType: KeepOnNavigationType,
    paddingValue: PaddingValues,
) {
    NavHost(
        modifier = Modifier
            .fillMaxSize(),
        navController = navController,
        startDestination = NavigationDestination.Home.route,
    ) {
        composable(
            route = NavigationDestination.Home.route,
            enterTransition = defaultEnterTransition,
            exitTransition = defaultExitTransition,
        ) {
            HomeRoute(
                uiState = uiState,
                onEvent = onEvent,
                navType = navType,
                paddingValue = paddingValue,
            )
        }
        composable(
            route = NavigationDestination.Style.route,
            enterTransition = defaultEnterTransition,
            exitTransition = defaultExitTransition,
        ) {
            StyleRoute(
                uiState = uiState,
                onEvent = onEvent,
                navType = navType,
                paddingValue = paddingValue,
            )
        }
        composable(
            route = NavigationDestination.About.route,
            enterTransition = defaultEnterTransition,
            exitTransition = defaultExitTransition,
        ) {
            AboutRoute(
                appInfo = uiState.appInfo,
                navType = navType,
                paddingValue = paddingValue,
            )
        }
    }
}
