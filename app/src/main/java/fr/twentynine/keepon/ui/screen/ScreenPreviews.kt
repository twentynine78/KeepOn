package fr.twentynine.keepon.ui.screen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.twentynine.keepon.domain.catalog.CreditCatalog
import fr.twentynine.keepon.domain.model.AppInfo
import fr.twentynine.keepon.domain.model.ScreenTimeout
import fr.twentynine.keepon.domain.model.SpecialScreenTimeoutType
import fr.twentynine.keepon.domain.model.IconTransitionAnimation
import fr.twentynine.keepon.domain.model.TimeoutIconStyle
import fr.twentynine.keepon.ui.model.NeededPermission
import fr.twentynine.keepon.ui.model.ScreenTimeoutUI
import fr.twentynine.keepon.ui.theme.KeepOnTheme
import fr.twentynine.keepon.ui.util.KeepOnNavigationType

// Sample data for the @Preview functions below. Generated timeout icons render blank in previews
// (they need the Coil pipeline at runtime); previews are for layout/spacing/colors.
private val previewScreenTimeouts = listOf(
    ScreenTimeoutUI(value = 15000, displayName = "15 s", isSelected = false, isDefault = false, isCurrent = false, isLocked = false),
    ScreenTimeoutUI(value = 30000, displayName = "30 s", isSelected = true, isDefault = true, isCurrent = true, isLocked = false),
    ScreenTimeoutUI(value = 120000, displayName = "2 min", isSelected = true, isDefault = false, isCurrent = false, isLocked = false),
    ScreenTimeoutUI(value = Int.MAX_VALUE, displayName = "∞", isSelected = false, isDefault = false, isCurrent = false, isLocked = true),
)

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    KeepOnTheme(dynamicColor = false) {
        HomeScreen(
            tipsList = emptyList(),
            resetTimeoutWhenScreenOff = true,
            screenTimeouts = previewScreenTimeouts,
            timeoutIconStyle = TimeoutIconStyle(),
            isFirstLaunch = false,
            onEvent = {},
            navType = KeepOnNavigationType.BOTTOM_NAVIGATION,
            paddingValue = PaddingValues(0.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun StyleScreenPreview() {
    KeepOnTheme(dynamicColor = false) {
        StyleScreen(
            timeoutIconStyle = TimeoutIconStyle(),
            iconTransitionAnimation = IconTransitionAnimation(),
            onEvent = {},
            navType = KeepOnNavigationType.BOTTOM_NAVIGATION,
            paddingValue = PaddingValues(0.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AboutScreenPreview() {
    KeepOnTheme(dynamicColor = false) {
        AboutScreen(
            appInfo = AppInfo(
                version = "2.1.2",
                author = "TwentyNine",
                sourceCodeUrl = "https://github.com/TwentyNine78/KeepOn",
            ),
            creditInfoMap = CreditCatalog.creditInfoMap,
            navType = KeepOnNavigationType.BOTTOM_NAVIGATION,
            paddingValue = PaddingValues(0.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TaskerEditScreenPreview() {
    KeepOnTheme(dynamicColor = false) {
        TaskerEditScreen(
            screenTimeouts = previewScreenTimeouts,
            specialScreenTimeouts = listOf(
                ScreenTimeoutUI(
                    value = SpecialScreenTimeoutType.DEFAULT_SCREEN_TIMEOUT_TYPE.value,
                    displayName = "Default",
                    isSelected = false,
                    isDefault = false,
                    isCurrent = false,
                    isLocked = false,
                ),
                ScreenTimeoutUI(
                    value = SpecialScreenTimeoutType.PREVIOUS_SCREEN_TIMEOUT_TYPE.value,
                    displayName = "Previous",
                    isSelected = false,
                    isDefault = false,
                    isCurrent = false,
                    isLocked = false,
                ),
            ),
            defaultScreenTimeout = ScreenTimeout(30000),
            previousScreenTimeout = ScreenTimeout(15000),
            selectedScreenTimeout = previewScreenTimeouts[1],
            timeoutIconStyle = TimeoutIconStyle(),
            onEvent = {},
            paddingValue = PaddingValues(0.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ErrorScreenPreview() {
    KeepOnTheme(dynamicColor = false) {
        ErrorScreen(errorMessage = "Unable to read the screen timeout settings.")
    }
}

@Preview(showBackground = true)
@Composable
private fun PermissionRequestScreenPreview() {
    KeepOnTheme(dynamicColor = false) {
        PermissionRequestScreen(
            neededPermissionList = listOf(
                NeededPermission(
                    title = "Notifications",
                    description = "Allow KeepOn to post its ongoing notification.",
                    requestNeeded = false,
                    requestAction = {},
                ),
                NeededPermission(
                    title = "Battery optimization",
                    description = "Let KeepOn run reliably in the background.",
                    requestNeeded = true,
                    requestAction = {},
                ),
                NeededPermission(
                    title = "Modify system settings",
                    description = "Required to change the screen timeout.",
                    requestNeeded = true,
                    requestAction = {},
                ),
            ),
            updatePermissions = {},
        )
    }
}
