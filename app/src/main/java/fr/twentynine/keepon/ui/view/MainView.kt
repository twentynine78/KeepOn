package fr.twentynine.keepon.ui.view

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.BottomAppBarScrollBehavior
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil3.compose.AsyncImage
import fr.twentynine.keepon.R
import fr.twentynine.keepon.data.enums.TimeoutIconSize
import fr.twentynine.keepon.data.local.TipsInfo
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
import fr.twentynine.keepon.util.StringResourceProviderImpl

private fun NavigationSuiteType.toKeepOnNavType() = when (this) {
    NavigationSuiteType.NavigationBar -> KeepOnNavigationType.BOTTOM_NAVIGATION
    NavigationSuiteType.NavigationRail -> KeepOnNavigationType.NAVIGATION_RAIL
    else -> KeepOnNavigationType.BOTTOM_NAVIGATION
}

private const val SLIDE_ANIMATION_DURATION_MS = 300
private const val FAB_ANIMATION_DURATION_MS = 50
private const val FAB_CORNER_RADIUS = 24
private const val FAB_SIZE = 68
private const val FAB_ICON_SIZE = 40

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
fun MainView(
    uiState: MainViewUIState,
    onEvent: (MainUIEvent) -> Unit,
) {
    val targetScreenState by remember(uiState) {
        derivedStateOf {
            when (uiState) {
                is MainViewUIState.Error -> MainScreenState.ERROR
                is MainViewUIState.Success -> {
                    if (!uiState.canWriteSystemSettings || !uiState.batteryIsNotOptimized) {
                        MainScreenState.PERMISSION
                    } else {
                        MainScreenState.KEEP_ON
                    }
                }
                else -> MainScreenState.EMPTY
            }
        }
    }

    AnimatedContent(
        modifier = Modifier.fillMaxSize(),
        targetState = targetScreenState,
        transitionSpec = {
            when {
                targetState == MainScreenState.KEEP_ON && initialState == MainScreenState.PERMISSION ->
                    slideInFromRight togetherWith slideOutToLeft
                targetState == MainScreenState.PERMISSION && initialState == MainScreenState.KEEP_ON ->
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
                    ErrorView(errorMessage = uiState.error)
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

    val topBarScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val bottomBarScrollBehavior = BottomAppBarDefaults.exitAlwaysScrollBehavior()

    val colorScheme = MaterialTheme.colorScheme
    val primaryContainerColor = colorScheme.primaryContainer
    val backgroundColor = colorScheme.background
    val onBackgroundColor = colorScheme.onBackground
    val onPrimaryContainerColor = colorScheme.onPrimaryContainer

    KeepOnNavigationWrapper(
        topLevelDestinations = topLevelDestinations,
        selectedDestination = selectedDestination,
        keepOnIsActive = uiState.keepOnIsActive,
        currentScreenTimeout = uiState.currentScreenTimeout,
        timeoutIconStyle = uiState.timeoutIconStyle,
        fabOnClick = { onEvent(MainUIEvent.SetNextSelectedSystemScreenTimeout) },
        scrollBehavior = bottomBarScrollBehavior,
        navigateToTopLevelDestination = navigationActions::navigateTo,
    ) {
        val navType = navSuiteType.toKeepOnNavType()

        val localDensity = LocalDensity.current
        var scaffoldWidthDp by remember { mutableStateOf(0.dp) }

        val animationDuration = remember { FAB_ANIMATION_DURATION_MS }

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

        val scaffoldModifier = Modifier.getScaffoldModifier(
            localDensity = localDensity,
            scaffoldWidthDp = scaffoldWidthDp,
            onScaffoldWidthChanged = { scaffoldWidthDp = it },
            navType = navType,
            topBarScrollBehavior = topBarScrollBehavior,
            bottomBarScrollBehavior = bottomBarScrollBehavior
        )

        Scaffold(
            modifier = scaffoldModifier,
            contentWindowInsets = WindowInsets.safeDrawing,
            containerColor = backgroundColor,
            topBar = {
                KeepOnTopAppBar(
                    scrollBehavior = topBarScrollBehavior,
                    backgroundColor = colorScheme.background,
                    onBackgroundColor = colorScheme.onBackground
                )
            },
            floatingActionButton = {
                if (navType == KeepOnNavigationType.BOTTOM_NAVIGATION) {
                    KeepOnFloatingActionButton(
                        uiState = uiState,
                        contentColor = fabContentColor,
                        borderColor = fabBorderColor,
                        backgroundColor = fabBackgroundColor,
                        onEvent = onEvent
                    )
                }
            },
        ) { paddingValue ->
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
private fun Modifier.getScaffoldModifier(
    localDensity: Density,
    scaffoldWidthDp: Dp,
    onScaffoldWidthChanged: (Dp) -> Unit,
    navType: KeepOnNavigationType,
    topBarScrollBehavior: TopAppBarScrollBehavior,
    bottomBarScrollBehavior: BottomAppBarScrollBehavior
): Modifier {
    return this
        .fillMaxSize()
        .onGloballyPositioned { coordinates ->
            val newWidthDp = with(localDensity) { coordinates.size.width.toDp() }
            if (scaffoldWidthDp != newWidthDp) {
                onScaffoldWidthChanged(newWidthDp)
            }
        }
        .padding(
            start = getStartPaddingForDisplayCutout(scaffoldWidthDp, navType),
            end = getEndPaddingForDisplayCutout(scaffoldWidthDp),
            bottom = getBottomPadding(navType),
        )
        .nestedScroll(topBarScrollBehavior.nestedScrollConnection)
        .nestedScroll(bottomBarScrollBehavior.nestedScrollConnection)
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
            actionIconContentColor = onBackgroundColor
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
private fun KeepOnFloatingActionButton(
    uiState: MainViewUIState.Success,
    contentColor: Color,
    borderColor: Color,
    backgroundColor: Color,
    onEvent: (MainUIEvent) -> Unit,
) {
    val context = LocalContext.current
    val stringResourceProvider = remember { StringResourceProviderImpl(context) }
    val imageDescription by remember {
        derivedStateOf {
            uiState.currentScreenTimeout.getFullDisplayTimeout(stringResourceProvider)
        }
    }
    val imageData = remember(uiState.currentScreenTimeout, uiState.timeoutIconStyle) {
        TimeoutIconData(
            uiState.currentScreenTimeout,
            TimeoutIconSize.LARGE,
            uiState.timeoutIconStyle
        )
    }

    FloatingActionButton(
        modifier = Modifier
            .border(
                width = 1.dp,
                color = borderColor,
                RoundedCornerShape(FAB_CORNER_RADIUS.dp)
            )
            .size(FAB_SIZE.dp),
        onClick = { onEvent(MainUIEvent.SetNextSelectedSystemScreenTimeout) },
        containerColor = backgroundColor,
        contentColor = contentColor,
        shape = RoundedCornerShape(FAB_CORNER_RADIUS.dp),
    ) {
        AsyncImage(
            modifier = Modifier
                .size(FAB_ICON_SIZE.dp, FAB_ICON_SIZE.dp)
                .padding(bottom = 2.dp),
            model = imageData,
            colorFilter = ColorFilter.tint(contentColor),
            contentDescription = imageDescription,
        )
    }
}

@Composable
private fun KeepOnContent(
    paddingValue: PaddingValues,
    navController: NavHostController,
    uiState: MainViewUIState.Success,
    onEvent: (MainUIEvent) -> Unit,
    navType: KeepOnNavigationType
) {
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
        modifier = screenPaddingModifier.fillMaxSize(),
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

@Composable
private fun getStartPaddingForDisplayCutout(
    boxWidthDp: Dp,
    navType: KeepOnNavigationType,
): Dp {
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current

    val rawDevicePadding = with(density) {
        val safeDrawingInsets = WindowInsets.safeDrawing
        when (layoutDirection) {
            LayoutDirection.Ltr -> safeDrawingInsets.getLeft(this, layoutDirection).toDp()
            LayoutDirection.Rtl -> safeDrawingInsets.getRight(this, layoutDirection).toDp()
        }
    }

    return if (navType == KeepOnNavigationType.BOTTOM_NAVIGATION) {
        val maxContentWidthWithPadding = MAX_SCREEN_CONTENT_WIDTH_IN_DP.dp + (rawDevicePadding * 2)
        if (boxWidthDp <= maxContentWidthWithPadding) {
            rawDevicePadding
        } else {
            0.dp
        }
    } else {
        0.dp
    }
}

@Composable
private fun getEndPaddingForDisplayCutout(
    boxWidthDp: Dp,
): Dp {
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current

    val rawDevicePadding = with(density) {
        val safeDrawingInsets = WindowInsets.safeDrawing
        when (layoutDirection) {
            LayoutDirection.Ltr -> safeDrawingInsets.getRight(this, layoutDirection).toDp()
            LayoutDirection.Rtl -> safeDrawingInsets.getLeft(this, layoutDirection).toDp()
        }
    }

    return if (boxWidthDp <= MAX_SCREEN_CONTENT_WIDTH_IN_DP.dp + (rawDevicePadding * 2)) {
        rawDevicePadding
    } else {
        0.dp
    }
}

@Composable
private fun getBottomPadding(
    navType: KeepOnNavigationType
): Dp {
    val density = LocalDensity.current

    val rawDevicePadding = with(density) {
        WindowInsets.displayCutout.getBottom(this).toDp()
    }

    return if (navType == KeepOnNavigationType.BOTTOM_NAVIGATION) {
        0.dp
    } else {
        rawDevicePadding
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
            enterTransition = defaultEnterTransition,
            exitTransition = defaultExitTransition,
        ) {
            HomeView(
                uiState = uiState,
                onEvent = onEvent,
                navType = navType,
            )
        }
        composable(
            route = NavigationDestination.Style.route,
            enterTransition = defaultEnterTransition,
            exitTransition = defaultExitTransition,
        ) {
            StyleView(
                uiState = uiState,
                onEvent = onEvent,
                navType = navType,
            )
        }
        composable(
            route = NavigationDestination.About.route,
            enterTransition = defaultEnterTransition,
            exitTransition = defaultExitTransition,
        ) {
            AboutView(navType = navType)
        }
    }
}
