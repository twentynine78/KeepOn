package fr.twentynine.keepon.data.model

import androidx.compose.runtime.Stable
import fr.twentynine.keepon.data.local.IconFontFamily
import kotlinx.serialization.Serializable

@Stable
@Serializable
data class TimeoutIconStyle(
    val iconStyleFontSize: Int = 0,
    val iconStyleFontHorizontalSpacing: Int = 0,
    val iconStyleFontVerticalSpacing: Int = 0,
    val iconFontFamilyName: String = IconFontFamily.Roboto.name,
    val iconStyleFontBold: Boolean = false,
    val iconStyleFontItalic: Boolean = false,
    val iconStyleFontUnderline: Boolean = false,
    val iconStyleTextOutlined: Boolean = false
)
