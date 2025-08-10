package fr.twentynine.keepon.data.model

import androidx.compose.runtime.Stable
import fr.twentynine.keepon.R
import fr.twentynine.keepon.data.enums.SpecialScreenTimeoutType
import fr.twentynine.keepon.util.StringResourceProvider
import kotlinx.serialization.Serializable

@Stable
@Serializable
data class ScreenTimeout(
    val value: Int
) {
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
