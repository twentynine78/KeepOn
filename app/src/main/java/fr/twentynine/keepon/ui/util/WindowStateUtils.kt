package fr.twentynine.keepon.ui.util

/**
 * Different type of navigation supported by app depending on device size and state.
 */
enum class KeepOnNavigationType {
    BOTTOM_NAVIGATION, NAVIGATION_RAIL
}

/**
 * Different position of navigation content inside Navigation Rail, Navigation Drawer depending on device size and state.
 */
enum class KeepOnNavigationContentPosition {
    TOP, CENTER
}

const val MAX_SCREEN_CONTENT_WIDTH_IN_DP = 650
