package fr.twentynine.keepon.ui.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarScrollBehavior
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldLayout
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import fr.twentynine.keepon.domain.model.IconTransitionAnimation
import fr.twentynine.keepon.domain.model.ScreenTimeout
import fr.twentynine.keepon.domain.model.TimeoutIconStyle
import fr.twentynine.keepon.ui.component.TimeoutFab
import fr.twentynine.keepon.ui.util.KeepOnNavigationContentPosition

/**
 * Adaptive navigation host: picks a bottom navigation bar or a navigation rail from the current
 * window size/posture and wraps [content] in a [KeepOnNavSuiteScope] exposing that choice. The rail
 * variant hosts the timeout FAB itself (the bar variant gets it from the Scaffold instead).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeepOnNavigationWrapper(
    topLevelDestinations: List<NavigationDestinationWithBadge>,
    selectedDestination: String,
    navigateToTopLevelDestination: (NavigationDestination) -> Unit,
    keepOnIsActive: Boolean,
    currentScreenTimeout: ScreenTimeout,
    currentTimeoutDisplay: String,
    timeoutIconStyle: TimeoutIconStyle,
    iconTransitionAnimation: IconTransitionAnimation,
    fabOnClick: () -> Unit,
    scrollBehavior: BottomAppBarScrollBehavior,
    content: @Composable KeepOnNavSuiteScope.() -> Unit
) {
    // Get adaptive screen info
    val adaptiveInfo = currentWindowAdaptiveInfo()

    val navLayoutType = when {
        adaptiveInfo.windowPosture.isTabletop -> NavigationSuiteType.NavigationBar
        adaptiveInfo.windowSizeClass.isWidthAtLeastBreakpoint(
            WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND
        ) -> NavigationSuiteType.NavigationRail
        else -> NavigationSuiteType.NavigationBar
    }
    val navContentPosition = when {
        adaptiveInfo.windowSizeClass.isHeightAtLeastBreakpoint(
            WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND
        ) -> KeepOnNavigationContentPosition.CENTER
        else -> KeepOnNavigationContentPosition.TOP
    }

    NavigationSuiteScaffoldLayout(
        layoutType = navLayoutType,
        navigationSuite = {
            when (navLayoutType) {
                NavigationSuiteType.NavigationBar -> BottomNavigationBar(
                    topLevelDestinations = topLevelDestinations,
                    selectedDestination = selectedDestination,
                    navigateToTopLevelDestination = navigateToTopLevelDestination,
                    scrollBehavior = scrollBehavior,
                )
                NavigationSuiteType.NavigationRail -> KeepOnNavigationRail(
                    topLevelDestinations = topLevelDestinations,
                    selectedDestination = selectedDestination,
                    navigateToTopLevelDestination = navigateToTopLevelDestination,
                    keepOnIsActive = keepOnIsActive,
                    currentScreenTimeout = currentScreenTimeout,
                    currentTimeoutDisplay = currentTimeoutDisplay,
                    timeoutIconStyle = timeoutIconStyle,
                    iconTransitionAnimation = iconTransitionAnimation,
                    fabOnClick = fabOnClick,
                    navigationContentPosition = navContentPosition,
                )
            }
        }
    ) {
        KeepOnNavSuiteScope(navLayoutType).content()
    }
}

/** Compact-width navigation: the bottom app bar of top-level destinations (the FAB sits in the Scaffold). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomNavigationBar(
    topLevelDestinations: List<NavigationDestinationWithBadge>,
    selectedDestination: String,
    navigateToTopLevelDestination: (NavigationDestination) -> Unit,
    scrollBehavior: BottomAppBarScrollBehavior,
) {
    BottomAppBar(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp),
        scrollBehavior = scrollBehavior,
    ) {
        // Fade the items out as the bar collapses, reading the scroll state at draw time so the
        // per-frame fraction changes never recompose the bar content.
        val itemAlphaModifier = Modifier.graphicsLayer {
            alpha = (1f - scrollBehavior.state.collapsedFraction * 2f).coerceIn(0f, 1f)
        }
        topLevelDestinations.forEach { topLevelDestination ->
            val isSelected = selectedDestination == topLevelDestination.destination.route
            val badgeAmount = topLevelDestination.badgeAmount

            NavigationBarItem(
                modifier = itemAlphaModifier,
                selected = isSelected,
                onClick = { navigateToTopLevelDestination(topLevelDestination.destination) },
                icon = {
                    if ((badgeAmount != null) && (badgeAmount > 0)) {
                        BadgedBox(badge = {
                            Badge {
                                Text(badgeAmount.toString())
                            }
                        }) {
                            NavigationBarItemIcon(
                                isSelected = isSelected,
                                topLevelDestination = topLevelDestination.destination,
                            )
                        }
                    } else {
                        NavigationBarItemIcon(
                            isSelected = isSelected,
                            topLevelDestination = topLevelDestination.destination,
                        )
                    }
                },
                label = {
                    Text(text = stringResource(topLevelDestination.destination.iconTextId))
                },
                alwaysShowLabel = true,
            )
        }
    }
}

/** Medium/expanded-width navigation: the side rail of top-level destinations, hosting the timeout FAB. */
@Composable
fun KeepOnNavigationRail(
    topLevelDestinations: List<NavigationDestinationWithBadge>,
    selectedDestination: String,
    navigateToTopLevelDestination: (NavigationDestination) -> Unit,
    keepOnIsActive: Boolean,
    currentScreenTimeout: ScreenTimeout,
    currentTimeoutDisplay: String,
    timeoutIconStyle: TimeoutIconStyle,
    iconTransitionAnimation: IconTransitionAnimation,
    fabOnClick: () -> Unit,
    navigationContentPosition: KeepOnNavigationContentPosition,
) {
    NavigationRail(
        modifier = Modifier.fillMaxHeight(),
        containerColor = MaterialTheme.colorScheme.inverseOnSurface,
    ) {
        Column(
            modifier = Modifier.fillMaxHeight()
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(0.3f))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TimeoutFab(
                    keepOnIsActive = keepOnIsActive,
                    currentScreenTimeout = currentScreenTimeout,
                    currentTimeoutDisplay = currentTimeoutDisplay,
                    timeoutIconStyle = timeoutIconStyle,
                    iconTransitionAnimation = iconTransitionAnimation,
                    onClick = fabOnClick,
                    animationDurationMs = 200,
                    elevation = FloatingActionButtonDefaults.elevation(1.dp),
                )
            }

            Spacer(modifier = Modifier.weight(0.3f))

            if (navigationContentPosition == KeepOnNavigationContentPosition.CENTER) {
                Spacer(modifier = Modifier.weight(0.4f))
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                topLevelDestinations.forEach { topLevelDestination ->
                    val isSelected = selectedDestination == topLevelDestination.destination.route
                    val badgeAmount = topLevelDestination.badgeAmount

                    NavigationRailItem(
                        selected = isSelected,
                        onClick = { navigateToTopLevelDestination(topLevelDestination.destination) },
                        icon = {
                            if ((badgeAmount != null) && (badgeAmount > 0)) {
                                BadgedBox(badge = {
                                    Badge {
                                        Text(badgeAmount.toString())
                                    }
                                }) {
                                    NavigationBarItemIcon(
                                        isSelected = isSelected,
                                        topLevelDestination = topLevelDestination.destination,
                                    )
                                }
                            } else {
                                NavigationBarItemIcon(
                                    isSelected = isSelected,
                                    topLevelDestination = topLevelDestination.destination,
                                )
                            }
                        },
                        label = {
                            Text(text = stringResource(topLevelDestination.destination.iconTextId))
                        },
                        alwaysShowLabel = true,
                    )
                }
            }

            if (navigationContentPosition == KeepOnNavigationContentPosition.CENTER) {
                Spacer(modifier = Modifier.weight(0.5f))
            }

            Spacer(modifier = Modifier.weight(0.3f))
        }
    }
}

/** A nav item's icon that cross-fades between the destination's filled (selected) and outlined icon. */
@Composable
fun NavigationBarItemIcon(
    isSelected: Boolean,
    topLevelDestination: NavigationDestination,
) {
    AnimatedContent(
        targetState = isSelected,
        transitionSpec = { fadeIn(animationSpec = tween(500)).togetherWith(fadeOut(animationSpec = tween(1000))) },
    ) { isSelected ->
        Icon(
            imageVector = if (isSelected) topLevelDestination.selectedIcon else topLevelDestination.unSelectedIcon,
            contentDescription = stringResource(topLevelDestination.iconTextId)
        )
    }
}
