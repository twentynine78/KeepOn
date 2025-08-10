package fr.twentynine.keepon.ui.navigation

import androidx.compose.runtime.Stable

data class NavigationDestinationWithBadge(
    val destination: NavigationDestination,
    val badgeAmount: Int? = null
)

@Stable
fun NavigationDestination.withBadge(badgeAmount: Int?): NavigationDestinationWithBadge {
    return NavigationDestinationWithBadge(this, badgeAmount)
}
