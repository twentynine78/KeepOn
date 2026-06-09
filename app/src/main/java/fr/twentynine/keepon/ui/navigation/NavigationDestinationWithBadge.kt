package fr.twentynine.keepon.ui.navigation

import androidx.compose.runtime.Stable

/**
 * A [NavigationDestination] paired with an optional badge count for its nav item (e.g. the number of
 * active tips on Home); null means no badge.
 */
data class NavigationDestinationWithBadge(
    val destination: NavigationDestination,
    val badgeAmount: Int? = null
)

/** Wraps this destination with a [badgeAmount] badge. */
@Stable
fun NavigationDestination.withBadge(badgeAmount: Int?): NavigationDestinationWithBadge {
    return NavigationDestinationWithBadge(this, badgeAmount)
}
