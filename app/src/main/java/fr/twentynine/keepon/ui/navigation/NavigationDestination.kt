package fr.twentynine.keepon.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.Info
import androidx.compose.ui.graphics.vector.ImageVector
import fr.twentynine.keepon.R
import fr.twentynine.keepon.ui.theme.icons.HomeFilled
import fr.twentynine.keepon.ui.theme.icons.HomeOutlined
import fr.twentynine.keepon.ui.theme.icons.IconStyleFilled
import fr.twentynine.keepon.ui.theme.icons.IconStyleOutlined

sealed class NavigationDestination(
    val route: String,
    @param:StringRes val iconTextId: Int,
    val selectedIcon: ImageVector,
    val unSelectedIcon: ImageVector,
) {
    data object Home : NavigationDestination(
        "home",
        R.string.screen_home,
        HomeFilled,
        HomeOutlined,
    )
    data object Style : NavigationDestination(
        "style",
        R.string.screen_style,
        IconStyleFilled,
        IconStyleOutlined,
    )
    data object About : NavigationDestination(
        "about",
        R.string.screen_about,
        Icons.Filled.Info,
        Icons.Outlined.Info,
    )
}

val TOP_LEVEL_DESTINATIONS = listOf(
    NavigationDestination.Home,
    NavigationDestination.Style,
    NavigationDestination.About
)
