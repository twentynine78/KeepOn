package fr.twentynine.keepon.ui.util

import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val BottomNavigationContentBottomSpacing = 118.dp
private val NavigationRailContentBottomSpacing = 12.dp

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
