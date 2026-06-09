package fr.twentynine.keepon.ui.navigation

import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType

/**
 * Receiver scope for the navigation wrapper's content, exposing the resolved [navSuiteType] (bar vs
 * rail) so the content can adapt its layout to the current form factor.
 */
class KeepOnNavSuiteScope(
    val navSuiteType: NavigationSuiteType
)
