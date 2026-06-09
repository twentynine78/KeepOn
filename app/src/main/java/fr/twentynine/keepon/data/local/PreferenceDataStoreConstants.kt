package fr.twentynine.keepon.data.local

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

/**
 * The DataStore preference keys, split by store: the first group lives in the auto-backed-up store
 * (user settings that should survive a reinstall), the second in the device-local store (state tied
 * to this install). The `OLD_*` keys are the legacy key names still read by the migrating
 * repositories to carry pre-existing values forward; the stored key strings keep their historical
 * names for on-device compatibility regardless of the constant name.
 */
object PreferenceDataStoreConstants {
    // Backed up data
    val SELECTED_SCREEN_TIMEOUT = stringPreferencesKey("newSelectedTimeout")
    val OLD_SELECTED_SCREEN_TIMEOUT = stringPreferencesKey("selectedTimeout")
    val RESET_TIMEOUT_WHEN_SCREEN_OFF = booleanPreferencesKey("resetTimeoutWhenScreenOff")
    val OLD_RESET_TIMEOUT_WHEN_SCREEN_OFF = booleanPreferencesKey("resetTimeoutOnScreenOff")
    val OLD_TIMEOUT_ICON_STYLE = stringPreferencesKey("timeoutIconStyle")
    val TIMEOUT_ICON_STYLE = stringPreferencesKey("newTimeoutIconStyle")
    val ICON_TRANSITION_ANIMATION = stringPreferencesKey("iconTransitionAnimation")
    val DISMISSED_TIPS = stringPreferencesKey("dismissedTips")
    val APP_LAUNCH_COUNT = longPreferencesKey("appLaunchCount")
    val OLD_APP_REVIEW_ASKED = booleanPreferencesKey("appReviewAsked")
    val OLD_SKIP_INTRO = booleanPreferencesKey("skipIntro")
    val IS_FIRST_LAUNCH = booleanPreferencesKey("isFirstLaunch")
    val LAST_RUN_VERSION_CODE = longPreferencesKey("lastRunVersionCode")

    // No backed up data
    val DEFAULT_SCREEN_TIMEOUT = intPreferencesKey("originalTimeout")
    val CURRENT_SCREEN_TIMEOUT = intPreferencesKey("newValue")
    val PREVIOUS_SCREEN_TIMEOUT = intPreferencesKey("previousValue")
    val QSTILE_ADDED = booleanPreferencesKey("tileAdded")
}
