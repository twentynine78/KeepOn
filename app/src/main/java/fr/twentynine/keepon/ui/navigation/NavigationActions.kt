package fr.twentynine.keepon.ui.navigation

import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController

/** Navigation commands for the top-level destinations, wrapping a [NavHostController]. */
class NavigationActions(private val navController: NavHostController) {

    /** Navigates to [destination] as a single top-level tab switch (saving/restoring state, no stack build-up). */
    fun navigateTo(destination: NavigationDestination) {
        navController.navigate(destination.route) {
            // Pop up to the start destination of the graph to
            // avoid building up a large stack of destinations
            // on the back stack as users select items
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            // Avoid multiple copies of the same destination when
            // reselecting the same item
            launchSingleTop = true
            // Restore state when reselecting a previously selected item
            restoreState = true
        }
    }
}
