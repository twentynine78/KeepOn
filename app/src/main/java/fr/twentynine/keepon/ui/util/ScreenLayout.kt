package fr.twentynine.keepon.ui.util

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.captionBar
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.material3.BottomAppBarScrollBehavior
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * Clearance kept above the content's bottom edge so the last list item stays clear of the floating FAB
 * (FabSize 68dp + a little breathing room). Shared so every FAB list lands at the same spot: the
 * home/style/about lists add it on top of the live system-navigation-bar inset (see [bottomSpacerHeight],
 * their content has no bottom padding), while the Tasker list adds it on top of its content padding,
 * which already carries that inset.
 */
val FabBottomClearance = 76.dp
private val NavigationRailContentBottomSpacing = 12.dp

/** Mirrors the M3 BottomAppBar container height (BottomAppBarTokens.ContainerHeight, not public API). */
private val BottomNavigationBarHeight = 80.dp

/** Caps a screen's content to the app's max readable width (shared by every screen). */
val screenContentModifier: Modifier = Modifier.width(MAX_SCREEN_CONTENT_WIDTH_IN_DP.dp)

/**
 * Height of the trailing spacer that keeps the last list item clear of the bottom navigation
 * bar + FAB; the navigation-rail layout only needs a small gap. For bottom navigation the system
 * navigation-bar inset is read live (≈48dp with 3 buttons, ≈24dp or less in gesture mode) and the
 * FAB clearance is added on top, so the spacer is just tall enough in either navigation mode.
 */
@Composable
fun bottomSpacerHeight(navType: KeepOnNavigationType): Dp = when (navType) {
    KeepOnNavigationType.BOTTOM_NAVIGATION ->
        WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + FabBottomClearance
    KeepOnNavigationType.NAVIGATION_RAIL -> NavigationRailContentBottomSpacing
}

/**
 * Content insets for the bottom-navigation Scaffold (the one hosting the timeout FAB). That Scaffold
 * has no bottomBar slot, so it would add the full bottom system-bar inset to the FAB offset — but the
 * BottomAppBar (rendered separately by NavigationSuiteScaffoldLayout, which doesn't consume insets for
 * its content) already includes that inset in its height, so feeding it again lifts the FAB a
 * navigation-bar height too high. Here we hand the Scaffold only the part of the bottom inset the
 * collapsing bar no longer covers: max(0, systemBottom − currentBarHeight). While the bar is visible
 * its height exceeds the inset, so this is 0 and the FAB sits just above the bar; as the bar scrolls
 * away the value ramps up to the full inset, parking the FAB just above the system navigation bar.
 * The two regimes meet continuously (both equal the inset at the crossover), so the FAB glides.
 */
@OptIn(ExperimentalMaterial3Api::class)
fun bottomNavFabScaffoldInsets(
    contentInsets: WindowInsets,
    scrollBehavior: BottomAppBarScrollBehavior,
): WindowInsets = object : WindowInsets {
    override fun getLeft(density: Density, layoutDirection: LayoutDirection) =
        contentInsets.getLeft(density, layoutDirection)

    override fun getTop(density: Density) = contentInsets.getTop(density)

    override fun getRight(density: Density, layoutDirection: LayoutDirection) =
        contentInsets.getRight(density, layoutDirection)

    override fun getBottom(density: Density): Int {
        val state = scrollBehavior.state
        // heightOffsetLimit stays 0 until the bar first reports its size; treat that as fully expanded.
        if (state.heightOffsetLimit == 0f) return 0
        val barHeightPx = -state.heightOffsetLimit + state.heightOffset
        return (contentInsets.getBottom(density) - barHeightPx).coerceAtLeast(0f).roundToInt()
    }
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
