package fr.twentynine.keepon.ui.util

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.captionBar
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val BottomNavigationContentBottomSpacing = 118.dp
private val NavigationRailContentBottomSpacing = 12.dp

/** Mirrors the M3 BottomAppBar container height (BottomAppBarTokens.ContainerHeight, not public API). */
private val BottomNavigationBarHeight = 80.dp

/** Caps a screen's content to the app's max readable width (shared by every screen). */
val screenContentModifier: Modifier = Modifier.width(MAX_SCREEN_CONTENT_WIDTH_IN_DP.dp)

/**
 * Height of the trailing spacer that keeps the last list item clear of the bottom navigation
 * bar + FAB; the navigation-rail layout only needs a small gap.
 */
fun bottomSpacerHeight(navType: KeepOnNavigationType): Dp = when (navType) {
    KeepOnNavigationType.BOTTOM_NAVIGATION -> BottomNavigationContentBottomSpacing
    KeepOnNavigationType.NAVIGATION_RAIL -> NavigationRailContentBottomSpacing
}

/**
 * Worst-case visible content height between the app chrome: window height minus the safe-drawing
 * insets, the fully expanded top app bar and, for bottom navigation, the bottom bar container.
 * Every input is stable while scrolling (the collapsing bars only free extra space), so content
 * sized against this never resizes mid-scroll; it updates on rotation/resize/inset changes only.
 */
@Composable
fun stableViewportHeight(navType: KeepOnNavigationType): Dp {
    val density = LocalDensity.current
    val windowHeightPx = LocalWindowInfo.current.containerSize.height
    // Mirror the insets MainScreen feeds the Scaffold as contentWindowInsets.
    val insets = WindowInsets.safeDrawing.union(WindowInsets.captionBar)
    val insetsPx = insets.getTop(density) + insets.getBottom(density)
    val barHeight = when (navType) {
        KeepOnNavigationType.BOTTOM_NAVIGATION -> BottomNavigationBarHeight
        KeepOnNavigationType.NAVIGATION_RAIL -> 0.dp
    }
    return with(density) { (windowHeightPx - insetsPx).toDp() } -
        TopAppBarDefaults.TopAppBarExpandedHeight - barHeight
}
