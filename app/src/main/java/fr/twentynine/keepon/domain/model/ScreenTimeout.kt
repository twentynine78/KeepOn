package fr.twentynine.keepon.domain.model

import fr.twentynine.keepon.R
import fr.twentynine.keepon.domain.gateway.StringResourceProvider
import kotlinx.serialization.Serializable

/**
 * A screen timeout, as the raw system value in milliseconds. Also carries the two special sentinel
 * values ([SpecialScreenTimeoutType]) for "restore default" and "previous", and [Int.MAX_VALUE] for
 * the never-sleep case, which the display formatters resolve to their own labels.
 */
@Serializable
data class ScreenTimeout(
    val value: Int
) {
    /** Long, spelled-out label (e.g. "2 minutes", "Infinite") used in menus and tooltips. */
    fun getFullDisplayTimeout(stringResourceProvider: StringResourceProvider): String {
        return when {
            value == Int.MAX_VALUE ->
                stringResourceProvider.getString(R.string.qs_long_infinite)
            value >= 3600000 && (value % 3600000) == 0 -> {
                val nbHour = value / 3600000
                stringResourceProvider.getPlural(R.plurals.qs_long_hour, nbHour)
            }
            value >= 60000 && (value % 60000) == 0 -> {
                val nbMinute = value / 60000
                stringResourceProvider.getPlural(R.plurals.qs_long_minute, nbMinute)
            }
            value == SpecialScreenTimeoutType.DEFAULT_SCREEN_TIMEOUT_TYPE.value ->
                stringResourceProvider.getString(R.string.timeout_restore_long)
            value == SpecialScreenTimeoutType.PREVIOUS_SCREEN_TIMEOUT_TYPE.value ->
                stringResourceProvider.getString(R.string.timeout_previous_long)
            else -> {
                val nbSecond = value / 1000
                stringResourceProvider.getPlural(R.plurals.qs_long_second, nbSecond)
            }
        }
    }

    /** Compact label (e.g. "2m", "30s", "∞") used on the generated timeout icon and the chip. */
    fun getShortDisplayTimeout(stringResourceProvider: StringResourceProvider): String {
        return when {
            value == Int.MAX_VALUE ->
                stringResourceProvider.getString(R.string.qs_short_infinite)
            value >= 3600000 && (value % 3600000) == 0 ->
                buildString {
                    append(value / 3600000)
                    append(stringResourceProvider.getString(R.string.qs_short_hour))
                }
            value >= 60000 && (value % 60000) == 0 ->
                buildString {
                    append(value / 60000)
                    append(stringResourceProvider.getString(R.string.qs_short_minute))
                }
            else ->
                buildString {
                    append(value / 1000)
                    append(stringResourceProvider.getString(R.string.qs_short_second))
                }
        }
    }
}
