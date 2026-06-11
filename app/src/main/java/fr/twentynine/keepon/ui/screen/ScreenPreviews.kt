package fr.twentynine.keepon.ui.screen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.twentynine.keepon.domain.model.AppInfo
import fr.twentynine.keepon.domain.model.ScreenTimeout
import fr.twentynine.keepon.domain.model.SpecialScreenTimeoutType
import fr.twentynine.keepon.domain.model.IconTransitionAnimation
import fr.twentynine.keepon.domain.model.TimeoutIconStyle
import fr.twentynine.keepon.ui.model.CreditInfoUI
import fr.twentynine.keepon.ui.model.CreditSectionUI
import fr.twentynine.keepon.ui.model.IconTransitionOptionUI
import fr.twentynine.keepon.ui.model.NeededPermission
import fr.twentynine.keepon.ui.model.ScreenTimeoutUI
import fr.twentynine.keepon.ui.theme.KeepOnTheme
import fr.twentynine.keepon.ui.util.KeepOnNavigationType

// Sample data for the @Preview functions below. Generated timeout icons render blank in previews
// (they need the Coil pipeline at runtime); previews are for layout/spacing/colors.
private val previewScreenTimeouts = listOf(
    ScreenTimeoutUI(value = 15000, displayName = "15 seconds", isSelected = false, isDefault = false, isCurrent = false, isLocked = false),
    ScreenTimeoutUI(value = 30000, displayName = "30 seconds", isSelected = true, isDefault = true, isCurrent = true, isLocked = false),
    ScreenTimeoutUI(value = 60000, displayName = "1 minute", isSelected = true, isDefault = false, isCurrent = false, isLocked = false),
    ScreenTimeoutUI(value = 120000, displayName = "2 minutes", isSelected = true, isDefault = false, isCurrent = false, isLocked = false),
    ScreenTimeoutUI(value = 300000, displayName = "5 minutes", isSelected = true, isDefault = false, isCurrent = false, isLocked = false),
    ScreenTimeoutUI(value = 1800000, displayName = "30 minutes", isSelected = true, isDefault = false, isCurrent = false, isLocked = false),
    ScreenTimeoutUI(value = 3600000, displayName = "1 hour", isSelected = true, isDefault = false, isCurrent = false, isLocked = true),
    ScreenTimeoutUI(value = Int.MAX_VALUE, displayName = "Indefinitely", isSelected = false, isDefault = false, isCurrent = false, isLocked = true),
)

private val previewCreditSections = listOf(
    CreditSectionUI(
        typeName = "Library",
        credits = listOf(
            CreditInfoUI(name = "Coil", author = "Instacart team", url = "https://github.com/coil-kt/coil", version = "2.5.0"),
        ),
    ),
    CreditSectionUI(
        typeName = "Font",
        credits = listOf(
            CreditInfoUI(name = "Roboto", author = "Christian Robertson", url = "https://fonts.google.com/specimen/Roboto", version = null),
        ),
    ),
)

private val previewIconTransitionOptions = listOf(
    IconTransitionOptionUI(id = "liquid_morph", label = "Liquid morph"),
    IconTransitionOptionUI(id = "particles", label = "Particles"),
    IconTransitionOptionUI(id = "warp", label = "Turbulent warp"),
    IconTransitionOptionUI(id = "vortex", label = "Vortex"),
    IconTransitionOptionUI(id = "flip", label = "Flip"),
    IconTransitionOptionUI(id = "swipe_down", label = "Reel"),
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
            showFirstLaunchHint = false,
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
            iconTransitionOptions = previewIconTransitionOptions,
            currentScreenTimeout = ScreenTimeout(30000),
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
            creditSections = previewCreditSections,
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
